package net.lopymine.ip.family.cache;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(NativeImageExtension.class)
public class FamilyParticlesAtlasCacheManager {

	// namespace -> itemId -> particleId -> image
	private static final Map<String, Map<Identifier, Map<Identifier, NativeImage>>> NAMESPACE_TEXTURES = new ConcurrentHashMap<>();
	// itemId -> particleId(s)
	private static final Map<Identifier, List<Identifier>> ITEM_TEXTURES = new ConcurrentHashMap<>();

	private static final Map<String, Object> NAMESPACE_WRITE_LOCKS = new ConcurrentHashMap<>();
	private static final int MAX_ATLAS_WIDTH = 1024;

	private static Object lock(String namespace) {
		return NAMESPACE_WRITE_LOCKS.computeIfAbsent(namespace, ignoredNamespace -> new Object());
	}

	public static void save(String namespace) {
		Map<Identifier, Map<Identifier, NativeImage>> map = NAMESPACE_TEXTURES.get(namespace);
		if (map == null) {
			return;
		}

		List<AtlasImageRegion> regions = new ArrayList<>();
		for (Entry<Identifier, Map<Identifier, NativeImage>> e : map.entrySet()) {
			Identifier itemId = e.getKey();
			for (Entry<Identifier, NativeImage> entry : e.getValue().entrySet()) {
				regions.add(new AtlasImageRegion(itemId, entry.getKey(), entry.getValue()));
			}
		}

		if (regions.isEmpty()) {
			return;
		}

		Path dir = FamilyParticlesCacheManager.FOLDER.resolve(namespace);

		FamilySafeRenderExecutor.submit(() -> {
			synchronized (lock(namespace)) {
				try {
					Files.createDirectories(dir);

					List<PackedAtlasImageRegion> packedRegions = packRegions(regions);
					savePng(
							dir.resolve("atlas.png"),
							packedRegions
					);
					saveRegions(
							dir.resolve("regions.txt"),
							packedRegions
					);
				} catch (IOException exception) {
					InventoryParticlesClient.LOGGER.error(
							"Failed to cache particles atlas with id {}, reason:",
							namespace,
							exception
					);
				}
			}
		});
	}

	private static void savePng(Path pngPath, List<PackedAtlasImageRegion> packedRegions) throws IOException {
		int atlasWidth = 0;
		int atlasHeight = 0;

		for (PackedAtlasImageRegion region : packedRegions) {
			atlasWidth = Math.max(atlasWidth, region.x() + region.width());
			atlasHeight = Math.max(atlasHeight, region.y() + region.height());
		}

		try (NativeImage atlasImage = new NativeImage(atlasWidth, atlasHeight, false)) {
			for (PackedAtlasImageRegion region : packedRegions) {
				NativeImage image = region.image();

				for (int y = 0; y < region.height(); y++) {
					for (int x = 0; x < region.width(); x++) {
						atlasImage.setPixelArgb(
								region.x() + x,
								region.y() + y,
								image.getPixelArgb(x, y)
						);
					}
				}
			}

			atlasImage.writeToFile(pngPath);
		}
	}

	private static void saveRegions(Path txtPath, List<PackedAtlasImageRegion> packedRegions) throws IOException {
		try (Writer output = Files.newBufferedWriter(txtPath)) {
			for (PackedAtlasImageRegion region : packedRegions) {
				output.write(String.format(
						Locale.ROOT,
						"%s\t%s\t%d\t%d\t%d\t%d%n",
						region.itemId(),
						region.id(),
						region.x(),
						region.y(),
						region.width(),
						region.height()
				));
			}
		}
	}

	private static List<PackedAtlasImageRegion> packRegions(List<AtlasImageRegion> regions) {
		List<AtlasImageRegion> sortedRegions = regions.stream()
				.sorted(Comparator.comparing(AtlasImageRegion::id))
				.toList();

		List<PackedAtlasImageRegion> packedRegions = new ArrayList<>(sortedRegions.size());

		int x = 0;
		int y = 0;
		int rowHeight = 0;

		for (AtlasImageRegion region : sortedRegions) {
			NativeImage image = region.image();

			int width = image.getWidth();
			int height = image.getHeight();

			if (x > 0 && x + width > FamilyParticlesAtlasCacheManager.MAX_ATLAS_WIDTH) {
				x = 0;
				y += rowHeight;
				rowHeight = 0;
			}

			packedRegions.add(new PackedAtlasImageRegion(
					region.itemId(),
					region.id(),
					image,
					x,
					y,
					width,
					height
			));

			x += width;
			rowHeight = Math.max(rowHeight, height);
		}

		return packedRegions;
	}

	@Nullable
	public static TempPair load(String namespace) {
		try {
			InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
			if (!config.isTest220()) {
				FamilyParticlesCacheManager.delete();
				config.setTest220(true);
				config.save();
			} else {
				if (!config.isTest230()) {
					FamilyParticlesCacheManager.delete230();
					config.setTest230(true);
					config.save();
				}
			}
			if (!Files.exists(FamilyParticlesCacheManager.FOLDER)) {
				return null;
			}

			synchronized (lock(namespace)) {
				return loadNamespace(namespace);
			}
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to load cached particles atlas, reason:", e);
		}
		return null;
	}

	@Nullable
	private static TempPair loadNamespace(String namespace) {
		if (!Files.exists(FamilyParticlesCacheManager.FOLDER.resolve(namespace))) {
			return null;
		}

		NativeImage atlasImage = loadAtlas(namespace);
		if (atlasImage == null) {
			return null;
		}

		String atlasRegions = loadRegions(namespace);
		if (atlasRegions == null) {
			return null;
		}

		return parseSprites(atlasImage, atlasRegions);
	}

	private static @Nullable String loadRegions(String namespace) {
		Path regionsPath = FamilyParticlesCacheManager.FOLDER.resolve(namespace).resolve("regions.txt");
		String atlasRegions;
		try (InputStream inputStream = Files.newInputStream(regionsPath)) {
			atlasRegions = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error(
					"Failed to load regions from cached particles atlas, path {}, reason:",
					regionsPath,
					e
			);
			return null;
		}
		return atlasRegions;
	}

	private static @Nullable NativeImage loadAtlas(String namespace) {
		Path imagePath = FamilyParticlesCacheManager.FOLDER.resolve(namespace).resolve("atlas.png");
		NativeImage atlasImage;
		try (InputStream inputStream = Files.newInputStream(imagePath)) {
			atlasImage = NativeImage.read(inputStream);
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error(
					"Failed to load image of cached particles atlas, path {}, reason:",
					imagePath,
					e
			);
			return null;
		}
		return atlasImage;
	}

	private static TempPair parseSprites(NativeImage image, String regions) {
		Map<Identifier, Map<Identifier, NativeImage>> first = new HashMap<>();
		Map<Identifier, NativeImage> second = new HashMap<>();

		for (String line : regions.split("\n")) {
			try {
				String[] split = line.split("\t");

				if (split.length == 6) {
					Identifier itemId = InventoryParticles.parseId(split[0]);
					Identifier id = InventoryParticles.parseId(split[1]);
					int x = Integer.parseInt(split[2].strip());
					int y = Integer.parseInt(split[3].strip());
					int w = Integer.parseInt(split[4].strip());
					int h = Integer.parseInt(split[5].strip());

					NativeImage particleTexture = new NativeImage(w, h, false);
					image.copyRect(particleTexture, x, y, 0, 0, w, h, false, false);

					first.computeIfAbsent(itemId, (ignored) -> new HashMap<>()).put(id, particleTexture);
					continue;
				}

				if (split.length == 5) {
					Identifier id = InventoryParticles.parseId(split[0]);
					int x = Integer.parseInt(split[1].strip());
					int y = Integer.parseInt(split[2].strip());
					int w = Integer.parseInt(split[3].strip());
					int h = Integer.parseInt(split[4].strip());

					NativeImage particleTexture = new NativeImage(w, h, false);
					image.copyRect(particleTexture, x, y, 0, 0, w, h, false, false);

					second.put(id, particleTexture);
					continue;
				}
			} catch (Exception ignored) { }
		}
		image.close();
		return new TempPair(first, second);
	}

	public static void add(Identifier itemId, Identifier particleId, NativeImage particleImage) {
		ITEM_TEXTURES.computeIfAbsent(itemId, (ignored) -> new ArrayList<>()).add(particleId);
		NAMESPACE_TEXTURES.computeIfAbsent(itemId.getNamespace(), (ignored) -> new HashMap<>())
				.computeIfAbsent(itemId, (ignored) -> new HashMap<>())
				.put(particleId, particleImage);
	}

	public static void clear() {
		for (Map<Identifier, Map<Identifier, NativeImage>> value : NAMESPACE_TEXTURES.values()) {
			for (Identifier id : value.keySet()) {
				Minecraft.getInstance().getTextureManager().release(id);
			}
		}
		NAMESPACE_TEXTURES.clear();
		ITEM_TEXTURES.clear();
	}

	public static Map<String, Map<Identifier, Map<Identifier, NativeImage>>> getNamespaceTextures() {
		return NAMESPACE_TEXTURES;
	}

	public static List<Identifier> getOrLoadItemTextures(Identifier itemId) {
		List<Identifier> list = ITEM_TEXTURES.get(itemId);
		if (list == null) {
			return load(itemId);
		}
		return list;
	}

	public static List<Identifier> load(Identifier itemId) {
		String namespace = itemId.getNamespace();
		Map<Identifier, Map<Identifier, NativeImage>> alreadyCreatedMap = NAMESPACE_TEXTURES.get(namespace);
		if (alreadyCreatedMap != null) {
			return null;
		}

		TempPair pair = load(namespace);
		if (pair == null) {
			return null;
		}

		for (Entry<Identifier, Map<Identifier, NativeImage>> e : pair.first().entrySet()) {
			Identifier parsedItemId = e.getKey();
			for (Entry<Identifier, NativeImage> entry : e.getValue().entrySet()) {
				//? if >=1.21.4 {
				if (BuiltInRegistries.ITEM.get(parsedItemId).isEmpty()) {
					continue;
				}
				//?} else {
				/*if (BuiltInRegistries.ITEM.get(parsedItemId) == Items.AIR) {
					continue;
				}
				*///?}
				Identifier particleId = entry.getKey();
				ITEM_TEXTURES.computeIfAbsent(parsedItemId, (ignored) -> new ArrayList<>()).add(particleId);
				NAMESPACE_TEXTURES.computeIfAbsent(namespace, (ignored) -> new HashMap<>())
						.computeIfAbsent(parsedItemId, (ignored) -> new HashMap<>())
						.put(particleId, entry.getValue());
			}
		}

		// todo remove this later pls
		for (Entry<Identifier, NativeImage> entry : pair.second().entrySet()) {
			Identifier parsedItemId = parseItemId(entry, namespace);
			if (parsedItemId == null) {
				continue;
			}

			//? if >=1.21.4 {
			if (BuiltInRegistries.ITEM.get(parsedItemId).isEmpty()) {
				continue;
			}
			//?} else {
			/*if (BuiltInRegistries.ITEM.get(parsedItemId) == Items.AIR) {
				continue;
			}
			*///?}

			Identifier particleId = entry.getKey();
			ITEM_TEXTURES.computeIfAbsent(parsedItemId, (ignored) -> new ArrayList<>()).add(particleId);
			NAMESPACE_TEXTURES.computeIfAbsent(namespace, (ignored) -> new HashMap<>())
					.computeIfAbsent(parsedItemId, (ignored) -> new HashMap<>())
					.put(particleId, entry.getValue());
		}

		return ITEM_TEXTURES.get(itemId);
	}

	@Nullable
	private static Identifier parseItemId(Entry<Identifier, NativeImage> entry, String atlasId) {
		Identifier id = entry.getKey();
		String path = id.getPath();
		if (path.isEmpty()) {
			return null;
		}
		int i = path.indexOf("/");
		if (i == -1) {
			return null;
		}
		String substring = path.substring(0, i);
		return InventoryParticles.parseId(atlasId + ":" + substring);
	}

	public record TempPair(Map<Identifier, Map<Identifier, NativeImage>> first, Map<Identifier, NativeImage> second) { }

	public record PackedAtlasImageRegion(
			Identifier itemId,
			Identifier id,
			NativeImage image,
			int x,
			int y,
			int width,
			int height
	) {
	}

	public record AtlasImageRegion(
			Identifier itemId,
			Identifier id,
			NativeImage image
	) {
	}
}