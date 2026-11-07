package net.lopymine.ip.editor;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.*;
import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.*;
import net.minecraft.server.packs.repository.PackRepository;
import org.jetbrains.annotations.Nullable;

@Getter
public class PackEditSession {

	public static final Path WORKSPACE_FOLDER = MossyLoader.getConfigDir()
			.resolve(InventoryParticles.MOD_ID.replace("_", "-"))
			.resolve("editor");

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Nullable
	private final ParticlesPackInfo source;
	private final String packName;
	private final String namespace;
	private final Path workFolder;

	@Nullable
	private EditorPackResources resources;
	@Nullable
	private List<EditorConfigEntry> packEntries;
	@Nullable
	private List<EditorConfigEntry> otherEntries;
	@Nullable
	private EditorItemsIndex itemsIndex;
	private int otherEntriesVersion = -1;

	private boolean dirty;

	private PackEditSession(@Nullable ParticlesPackInfo source, String packName, String namespace, Path workFolder) {
		this.source = source;
		this.packName = packName;
		this.namespace = namespace;
		this.workFolder = workFolder;
	}

	public static PackEditSession createNew(String packName) throws IOException {
		String namespace = PackNamespaceUtils.toNamespace(packName);
		Path workFolder = prepareWorkFolder(packName);

		Files.createDirectories(workFolder.resolve(ParticlesPackLayout.particlesFolder(namespace)));
		Files.writeString(workFolder.resolve("pack.mcmeta"), createPackMeta(packName), StandardCharsets.UTF_8);

		PackEditSession session = new PackEditSession(null, packName, namespace, workFolder);
		session.dirty = true;
		return session;
	}

	public static PackEditSession open(ParticlesPackInfo info) throws IOException {
		String packName = info.archive() ? getUnpackedPackName(info.fileName()) : info.fileName();
		Path workFolder = prepareWorkFolder(packName);

		if (info.archive()) {
			extractArchive(info.path(), workFolder);
		} else {
			copyFolder(info.path(), workFolder);
		}

		return new PackEditSession(info, packName, info.getMainNamespace(), workFolder);
	}

	private static String getUnpackedPackName(String fileName) {
		int index = fileName.lastIndexOf('.');
		String name = index == -1 ? fileName : fileName.substring(0, index);

		Set<String> usedPackNames = ParticlesPacksManager.getUsedPackNames();
		if (!usedPackNames.contains(name)) {
			return name;
		}

		int number = 2;
		while (usedPackNames.contains("%s_%s".formatted(name, number))) {
			number++;
		}
		return "%s_%s".formatted(name, number);
	}

	public void createResources() {
		if (this.resources == null) {
			this.resources = new EditorPackResources(this);
		}
	}

	public void reloadResources() {
		if (this.resources != null) {
			this.resources.reload();
		}
	}

	public void markDirty() {
		this.dirty = true;
		this.invalidateIndexes();
	}

	public void invalidateIndexes() {
		this.packEntries = null;
		this.itemsIndex = null;
	}

	public List<EditorConfigEntry> getPackEntries() {
		if (this.packEntries == null) {
			this.packEntries = EditorConfigsIndex.ofPack(this);
		}
		return this.packEntries;
	}

	public List<EditorConfigEntry> getOtherEntries() {
		int version = ParticlesConfigsManager.getVersion();
		if (this.otherEntries == null || this.otherEntriesVersion != version) {
			this.otherEntries = EditorConfigsIndex.ofOthers(this);
			this.otherEntriesVersion = version;
		}
		return this.otherEntries;
	}

	public EditorItemsIndex getItemsIndex() {
		if (this.itemsIndex == null) {
			this.itemsIndex = EditorItemsIndex.create(this);
		}
		return this.itemsIndex;
	}

	public Path getParticlesFolder() {
		return this.workFolder.resolve(ParticlesPackLayout.particlesFolder(this.namespace));
	}

	public Path getFamiliesFolder() {
		return this.workFolder.resolve(ParticlesPackLayout.familiesFolder(this.namespace));
	}

	public boolean hasLegacyLayout() {
		for (String namespace : this.getPackNamespaces()) {
			for (ParticlesPackLayout.LegacyFolder folder : ParticlesPackLayout.LEGACY_FOLDERS) {
				if (Files.exists(this.workFolder.resolve(folder.legacyFolder(namespace)))) {
					return true;
				}
			}
		}

		return false;
	}

	public void migrateLayout() throws IOException {
		for (String namespace : this.getPackNamespaces()) {
			for (ParticlesPackLayout.LegacyFolder folder : ParticlesPackLayout.LEGACY_FOLDERS) {
				Path legacyFolder = this.workFolder.resolve(folder.legacyFolder(namespace));
				if (!Files.exists(legacyFolder)) {
					continue;
				}
				moveFolder(legacyFolder, this.workFolder.resolve(folder.newFolder(namespace)));
			}
			this.migrateAtlasFile(namespace);
		}

		this.reloadResources();
		this.markDirty();
	}

	public Path save() throws IOException {
		Path target = ParticlesPacksManager.getPacksFolder().resolve(this.packName);
		copyFolder(this.workFolder, target);
		ParticlesPacksManager.clearScannedContents();
		this.dirty = false;
		return target;
	}

	public void saveAndApply() throws IOException {
		this.save();
		this.invalidateIndexes();

		PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
		repository.reload();

		List<String> selected = new ArrayList<>(repository.getSelectedIds());
		String packId = ParticlesPacksManager.FILE_PACK_PREFIX + this.packName;
		if (!selected.contains(packId)) {
			selected.add(packId);
		}
		repository.setSelected(selected);

		this.reloadResources();
		Minecraft.getInstance().reloadResourcePacks();
	}

	public void discard() {
		if (this.resources != null) {
			this.resources.close();
			this.resources = null;
		}
		deleteFolder(this.workFolder);
	}

	private Set<String> getPackNamespaces() {
		Set<String> namespaces = new LinkedHashSet<>();
		namespaces.add(this.namespace);

		Path assets = this.workFolder.resolve("assets");
		if (Files.isDirectory(assets)) {
			try (Stream<Path> stream = Files.list(assets)) {
				stream.filter(Files::isDirectory).forEach((path) -> namespaces.add(path.getFileName().toString()));
			} catch (IOException e) {
				InventoryParticlesClient.LOGGER.error("Failed to list namespaces of \"{}\":", this.workFolder, e);
			}
		}

		return namespaces;
	}

	private void migrateAtlasFile(String namespace) throws IOException {
		Path legacyAtlas = this.workFolder.resolve("%s/atlases/%s.json".formatted(ParticlesPackLayout.assetsFolder(namespace), ParticlesPackLayout.LEGACY_ATLAS_FILE_NAME));
		if (!Files.exists(legacyAtlas)) {
			return;
		}

		Path newAtlas = this.workFolder.resolve("%s/atlases/%s.json".formatted(ParticlesPackLayout.assetsFolder(namespace), ParticlesPackLayout.ATLAS_FILE_NAME));
		String content = Files.readString(legacyAtlas, StandardCharsets.UTF_8)
				.replace("\"%s\"".formatted(ParticlesPackLayout.LEGACY_ATLAS_FILE_NAME), "\"%s/%s\"".formatted(InventoryParticles.MOD_ID, ParticlesPackLayout.ATLAS_FILE_NAME));

		Files.createDirectories(newAtlas.getParent());
		Files.writeString(newAtlas, content, StandardCharsets.UTF_8);
		Files.delete(legacyAtlas);
	}

	private static Path prepareWorkFolder(String packName) throws IOException {
		Path workFolder = WORKSPACE_FOLDER.resolve(packName);
		deleteFolder(workFolder);
		Files.createDirectories(workFolder);
		return workFolder;
	}

	private static String createPackMeta(String packName) {
		//? if >=1.21.11 {
		MetadataSectionType<PackMetadataSection> type = PackMetadataSection.forPackType(PackType.CLIENT_RESOURCES);
		PackFormat format = PackFormat.of(SharedConstants.RESOURCE_PACK_FORMAT_MAJOR, SharedConstants.RESOURCE_PACK_FORMAT_MINOR);
		PackMetadataSection section = new PackMetadataSection(Component.literal(packName), format.minorRange());

		JsonObject root = new JsonObject();
		root.add(type.name(), type.codec().encodeStart(JsonOps.INSTANCE, section).getOrThrow());
		return GSON.toJson(root);
		//?} else {
		/*JsonObject pack = new JsonObject();
		pack.addProperty("description", packName);
		pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));

		JsonObject root = new JsonObject();
		root.add("pack", pack);
		return GSON.toJson(root);
		*///?}
	}

	private static void copyFolder(Path from, Path to) throws IOException {
		try (Stream<Path> stream = Files.walk(from)) {
			for (Path path : stream.toList()) {
				Path target = to.resolve(from.relativize(path).toString());
				if (Files.isDirectory(path)) {
					Files.createDirectories(target);
					continue;
				}
				Files.createDirectories(target.getParent());
				Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	private static void moveFolder(Path from, Path to) throws IOException {
		try (Stream<Path> stream = Files.walk(from)) {
			for (Path path : stream.filter(Files::isRegularFile).toList()) {
				Path target = to.resolve(from.relativize(path).toString());
				Files.createDirectories(target.getParent());
				Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		deleteFolder(from);
	}

	private static void extractArchive(Path archive, Path to) throws IOException {
		try (ZipFile zipFile = new ZipFile(archive.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				Path target = to.resolve(entry.getName()).normalize();
				if (!target.startsWith(to)) {
					continue;
				}
				if (entry.isDirectory()) {
					Files.createDirectories(target);
					continue;
				}

				Files.createDirectories(target.getParent());
				try (InputStream inputStream = zipFile.getInputStream(entry)) {
					Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private static void deleteFolder(Path folder) {
		if (!Files.exists(folder)) {
			return;
		}
		try (Stream<Path> stream = Files.walk(folder)) {
			for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete folder \"{}\":", folder, e);
		}
	}
}
