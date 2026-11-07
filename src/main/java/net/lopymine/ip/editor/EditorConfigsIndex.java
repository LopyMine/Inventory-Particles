package net.lopymine.ip.editor;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.editor.EditorConfigEntry.Source;
import net.lopymine.ip.element.texture.ITexture;
import net.lopymine.ip.resourcepack.manager.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class EditorConfigsIndex {

	public static List<EditorConfigEntry> ofPack(PackEditSession session) {
		List<EditorConfigEntry> entries = new ArrayList<>();

		Path assets = session.getWorkFolder().resolve("assets");
		if (!Files.isDirectory(assets)) {
			return entries;
		}

		try (Stream<Path> namespaces = Files.list(assets)) {
			for (Path namespaceFolder : namespaces.filter(Files::isDirectory).toList()) {
				String namespace = namespaceFolder.getFileName().toString();
				Path particles = session.getWorkFolder().resolve(ParticlesPackLayout.particlesFolder(namespace));
				if (!Files.isDirectory(particles)) {
					continue;
				}

				try (Stream<Path> files = Files.walk(particles)) {
					for (Path file : files.filter(EditorConfigsIndex::isConfigFile).toList()) {
						String path = particles.relativize(file).toString().replace('\\', '/');
						path = path.substring(0, path.length() - AbstractConfigsManager.CONFIG_EXTENSION.length());
						entries.add(createPackEntry(session, namespace, path, file));
					}
				}
			}
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to index particle configs of \"{}\":", session.getPackName(), e);
		}

		entries.sort(Comparator.comparing(EditorConfigEntry::getFullPath, String.CASE_INSENSITIVE_ORDER));
		return entries;
	}

	public static List<EditorConfigEntry> ofOthers(PackEditSession session) {
		Map<Identifier, String> sourcePacks = ParticlesConfigsManager.getInstance().getSourcePacks();
		String editedPackId = ParticlesPacksManager.FILE_PACK_PREFIX + session.getPackName();

		List<EditorConfigEntry> entries = new ArrayList<>();

		ParticlesConfigsManager.REGISTERED_CONFIGS.forEach((id, configs) -> {
			String sourcePackId = sourcePacks.getOrDefault(id, "unknown");
			if (sourcePackId.equals(editedPackId)) {
				return;
			}

			List<String> itemIds = new ArrayList<>();
			for (ParticleConfig config : configs) {
				itemIds.addAll(getItemIds(config));
			}

			Source source = sourcePackId.startsWith(ParticlesPacksManager.FILE_PACK_PREFIX) ? Source.OTHER_PACK : Source.MOD;
			entries.add(new EditorConfigEntry(id, id.getNamespace(), getShortPath(id), source, sourcePackId, itemIds, null));
		});

		entries.sort(Comparator.comparing(EditorConfigEntry::getFullPath, String.CASE_INSENSITIVE_ORDER));
		return entries;
	}

	@Nullable
	public static EditorConfigEntry findById(PackEditSession session, Identifier id) {
		for (EditorConfigEntry entry : session.getPackEntries()) {
			if (entry.id().equals(id)) {
				return entry;
			}
		}
		for (EditorConfigEntry entry : session.getOtherEntries()) {
			if (entry.id().equals(id)) {
				return entry;
			}
		}
		return null;
	}

	public static ParticleConfig deepCopy(ParticleConfig config) {
		JsonElement json = ParticleConfig.CODEC.encodeStart(JsonOps.INSTANCE, config)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/;
		return ParticleConfig.CODEC.decode(JsonOps.INSTANCE, json)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
	}

	@Nullable
	public static LoadedConfig load(EditorConfigEntry entry) {
		if (entry.file() == null) {
			List<ParticleConfig> configs = ParticlesConfigsManager.REGISTERED_CONFIGS.get(entry.id());
			if (configs == null || configs.isEmpty()) {
				return null;
			}
			return new LoadedConfig(deepCopy(configs.get(0)), new HashSet<>(ParticleConfig.VISUAL_KEYS));
		}

		try (BufferedReader reader = Files.newBufferedReader(entry.file(), StandardCharsets.UTF_8)) {
			JsonElement json = JsonParser.parseReader(reader);
			ParticleConfig config = ParticleConfig.CODEC.decode(JsonOps.INSTANCE, json)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
			Set<String> keys = json instanceof JsonObject object ? new HashSet<>(object.keySet()) : Set.of();
			return new LoadedConfig(config, keys);
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to read particle config from \"{}\":", entry.file(), e);
			return null;
		}
	}

	public static List<ITexture> loadTextures(EditorConfigEntry entry) {
		if (entry.file() == null) {
			List<ParticleConfig> configs = ParticlesConfigsManager.REGISTERED_CONFIGS.get(entry.id());
			return configs == null || configs.isEmpty() ? List.of() : configs.get(0).getTextures();
		}

		LoadedConfig loaded = load(entry);
		return loaded == null ? List.of() : loaded.config().getTextures();
	}

	public static List<String> getItemIds(ParticleConfig config) {
		List<String> itemIds = new ArrayList<>();
		for (ParticleHolder holder : config.getHolders()) {
			Either<CachedItem, Identifier> itemOrTag = holder.getItemOrTag();
			itemOrTag.ifLeft((cachedItem) -> itemIds.add(cachedItem.getId().toString()));
			itemOrTag.ifRight((tag) -> itemIds.add("#" + tag));
		}
		return itemIds;
	}

	private static EditorConfigEntry createPackEntry(PackEditSession session, String namespace, String path, Path file) {
		Identifier id = InventoryParticles.id(namespace, "%s/%s".formatted(ParticlesConfigsManager.getInstance().getFolderPath(), path));
		EditorConfigEntry entry = new EditorConfigEntry(id, namespace, path, Source.PACK, ParticlesPacksManager.FILE_PACK_PREFIX + session.getPackName(), new ArrayList<>(), file);

		LoadedConfig loaded = load(entry);
		if (loaded != null) {
			entry.itemIds().addAll(getItemIds(loaded.config()));
		}

		return entry;
	}

	private static String getShortPath(Identifier id) {
		String prefix = ParticlesConfigsManager.getInstance().getFolderPath() + "/";
		String path = id.getPath();
		return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
	}

	private static boolean isConfigFile(Path path) {
		return Files.isRegularFile(path) && path.getFileName().toString().endsWith(AbstractConfigsManager.CONFIG_EXTENSION);
	}

	public record LoadedConfig(ParticleConfig config, Set<String> keys) { }
}
