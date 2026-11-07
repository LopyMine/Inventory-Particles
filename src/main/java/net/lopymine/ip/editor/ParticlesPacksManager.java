package net.lopymine.ip.editor;

//? if >=26.3 {
import com.mojang.blaze3d.Blaze3D;
//?}
import com.mojang.blaze3d.platform.NativeImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.editor.ParticlesPackInfo.PackContent;
import net.lopymine.ip.family.FamilyParticlesConfigManager;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.*;
//? if <26.3 {
/*import net.minecraft.util.Util;
*///?}
import org.jetbrains.annotations.Nullable;

public class ParticlesPacksManager {

	public static final String FILE_PACK_PREFIX = "file/";

	private static final Identifier DEFAULT_ICON = InventoryParticles.id("minecraft", "textures/misc/unknown_pack.png");
	private static final String ICON_FILE_NAME = "pack.png";

	private static final Map<String, PackContent> SCANNED_CONTENTS = new HashMap<>();
	private static final Map<String, Identifier> ICONS = new HashMap<>();

	public static Path getPacksFolder() {
		return Minecraft.getInstance().getResourcePackDirectory();
	}

	public static Identifier getIcon(ParticlesPackInfo info) {
		return ICONS.computeIfAbsent(info.packId(), (packId) -> loadIcon(info));
	}

	public static void openInExplorer(ParticlesPackInfo info) {
		Path path = info.archive() ? info.path().getParent() : info.path();
		if (path == null) {
			return;
		}

		//? if >=26.3 {
		Blaze3D.openPath(path);
		//?} elif >=1.21.1 {
		/*Util.getPlatform().openPath(path);
		*///?} else {
		/*Util.getPlatform().openUri(path.toUri());
		*///?}
	}

	private static Identifier loadIcon(ParticlesPackInfo info) {
		try {
			byte[] bytes = readIcon(info);
			if (bytes == null) {
				return DEFAULT_ICON;
			}

			Identifier id = InventoryParticles.id("pack_icon/%s".formatted(Integer.toHexString(info.packId().hashCode())));
			//? if >=1.21.11 {
			Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, NativeImage.read(bytes)));
			//?} else {
			/*Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(NativeImage.read(bytes)));
			*///?}
			return id;
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.warn("Failed to read icon of resource pack \"{}\":", info.fileName(), e);
			return DEFAULT_ICON;
		}
	}

	private static byte @Nullable [] readIcon(ParticlesPackInfo info) throws IOException {
		if (!info.archive()) {
			Path path = info.path().resolve(ICON_FILE_NAME);
			return Files.isRegularFile(path) ? Files.readAllBytes(path) : null;
		}

		try (ZipFile zipFile = new ZipFile(info.path().toFile())) {
			ZipEntry entry = zipFile.getEntry(ICON_FILE_NAME);
			if (entry == null) {
				return null;
			}
			try (InputStream inputStream = zipFile.getInputStream(entry)) {
				return inputStream.readAllBytes();
			}
		}
	}

	public static void reloadPacks() {
		Minecraft.getInstance().getResourcePackRepository().reload();
	}

	public static List<ParticlesPackInfo> getEditablePacks() {
		PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
		Collection<String> selectedIds = repository.getSelectedIds();

		List<ParticlesPackInfo> packs = new ArrayList<>();
		for (Pack pack : repository.getAvailablePacks()) {
			String packId = pack.getId();
			if (!packId.startsWith(FILE_PACK_PREFIX)) {
				continue;
			}

			String fileName = packId.substring(FILE_PACK_PREFIX.length());
			Path path = getPacksFolder().resolve(fileName);
			if (!Files.exists(path)) {
				continue;
			}

			packs.add(new ParticlesPackInfo(
					packId,
					fileName,
					pack.getTitle(),
					path,
					Files.isRegularFile(path),
					selectedIds.contains(packId),
					SCANNED_CONTENTS.get(packId)
			));
		}

		packs.sort(Comparator.comparing(ParticlesPackInfo::fileName, String.CASE_INSENSITIVE_ORDER));
		return packs;
	}

	public static void scanAsync(List<ParticlesPackInfo> packs, Consumer<ParticlesPackInfo> onScanned) {
		for (ParticlesPackInfo info : packs) {
			if (info.isScanned()) {
				continue;
			}
			CompletableFuture.supplyAsync(() -> scan(info)).thenAcceptAsync(onScanned, Minecraft.getInstance());
		}
	}

	public static ParticlesPackInfo scan(ParticlesPackInfo info) {
		if (info.isScanned()) {
			return info;
		}

		PackContent content = info.archive() ? scanArchive(info.path()) : scanFolder(info.path());
		Minecraft.getInstance().execute(() -> SCANNED_CONTENTS.put(info.packId(), content));
		return info.withContent(content);
	}

	public static void clearScannedContents() {
		SCANNED_CONTENTS.clear();
		ICONS.clear();
	}

	public static Set<String> getUsedPackNames() {
		Set<String> names = new HashSet<>();
		try (Stream<Path> stream = Files.list(getPacksFolder())) {
			stream.forEach((path) -> names.add(path.getFileName().toString()));
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to list resource packs folder:", e);
		}
		return names;
	}

	private static PackContent scanArchive(Path path) {
		ContentScanner scanner = new ContentScanner();
		try (ZipFile zipFile = new ZipFile(path.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				scanner.accept(entries.nextElement().getName());
			}
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to read resource pack archive \"{}\":", path, e);
		}
		return scanner.toContent();
	}

	private static PackContent scanFolder(Path path) {
		ContentScanner scanner = new ContentScanner();
		try (Stream<Path> stream = Files.walk(path)) {
			stream.filter(Files::isRegularFile).forEach((file) -> scanner.accept(path.relativize(file).toString().replace('\\', '/')));
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to read resource pack folder \"{}\":", path, e);
		}
		return scanner.toContent();
	}

	private static class ContentScanner {

		private final Set<String> namespaces = new HashSet<>();
		private int particleConfigs;
		private int familyConfigs;
		private boolean legacyLayout;

		private PackContent toContent() {
			return new PackContent(this.namespaces, this.particleConfigs, this.familyConfigs, this.legacyLayout);
		}

		private void accept(String path) {
			if (!path.startsWith("assets/") || !ParticlesPackLayout.isConfigFile(path)) {
				return;
			}

			String[] parts = path.split("/");
			if (parts.length < 4) {
				return;
			}
			String namespace = parts[1];

			if (parts[2].equals(InventoryParticles.MOD_ID)) {
				if (parts[3].equals(ParticlesConfigsManager.FOLDER_NAME)) {
					this.particleConfigs++;
					this.namespaces.add(namespace);
				} else if (parts[3].equals(FamilyParticlesConfigManager.FOLDER_NAME)) {
					this.familyConfigs++;
					this.namespaces.add(namespace);
				}
				return;
			}

			if (!namespace.equals(InventoryParticles.MOD_ID)) {
				return;
			}

			if (parts[2].equals(ParticlesConfigsManager.LEGACY_FOLDER_NAME)) {
				this.particleConfigs++;
				this.namespaces.add(namespace);
				this.legacyLayout = true;
			} else if (parts[2].equals(FamilyParticlesConfigManager.LEGACY_FOLDER_NAME)) {
				this.familyConfigs++;
				this.namespaces.add(namespace);
				this.legacyLayout = true;
			}
		}
	}
}
