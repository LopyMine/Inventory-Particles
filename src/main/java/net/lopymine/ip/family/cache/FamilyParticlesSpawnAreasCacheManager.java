package net.lopymine.ip.family.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.element.mod.spawner.ParticleSpawnPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class FamilyParticlesSpawnAreasCacheManager {

	private static final int MAGIC = 0x49505058; // "IPPX"
	private static final int VERSION = 2;

	private static final Map<String, Map<String, List<ParticleSpawnPos>>> NAMESPACE_PIXELS = new ConcurrentHashMap<>();
	private static final Map<String, Object> NAMESPACE_WRITE_LOCKS = new ConcurrentHashMap<>();

	private static Object lock(String namespace) {
		return NAMESPACE_WRITE_LOCKS.computeIfAbsent(namespace, ignoredNamespace -> new Object());
	}

	public static void clear() {
		NAMESPACE_PIXELS.clear();
	}

	@Nullable
	public static List<ParticleSpawnPos> load(Identifier itemId) {
		Map<String, List<ParticleSpawnPos>> map = getOrLoadNamespacePixels(itemId.getNamespace());
		if (map == null) {
			return null;
		}
		return map.get(itemId.getPath());
	}

	@Nullable
	private static Map<String, List<ParticleSpawnPos>> getOrLoadNamespacePixels(String namespace) {
		Map<String, List<ParticleSpawnPos>> pixels = NAMESPACE_PIXELS.get(namespace);
		if (pixels == null) {
			synchronized (lock(namespace)) {
				pixels = NAMESPACE_PIXELS.get(namespace);

				if (pixels == null) {
					Path file = FamilyParticlesCacheManager.FOLDER.resolve(namespace).resolve("spawn_areas.cached");

					try {
						pixels = readNamespacePixels(file);
					} catch (IOException exception) {
						InventoryParticlesClient.LOGGER.error(
								"Failed to load cached pixels for namespace {}, reason:",
								namespace,
								exception
						);

						return null;
					}

					NAMESPACE_PIXELS.put(namespace, pixels);
				}
			}
		}
		return pixels;
	}

	private static Map<String, List<ParticleSpawnPos>> readNamespacePixels(Path file) throws IOException {
		Map<String, List<ParticleSpawnPos>> namespacePixels = new HashMap<>();

		if (!Files.isRegularFile(file)) {
			return namespacePixels;
		}

		try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
			int magic = inputStream.readInt();
			if (magic != MAGIC) {
				return namespacePixels;
			}

			int version = inputStream.readInt();
			if (version != VERSION) {
				return namespacePixels;
			}

			int entriesCount = inputStream.readInt();
			if (entriesCount < 0) {
				return namespacePixels;
			}

			for (int entryIndex = 0; entryIndex < entriesCount; entryIndex++) {
				String itemPath = inputStream.readUTF();

				int pixelsCount = inputStream.readInt();
				if (pixelsCount < 0) {
					return namespacePixels;
				}

				int width = inputStream.readInt();
				int height = inputStream.readInt();

				List<ParticleSpawnPos> pixels = new ArrayList<>(pixelsCount);

				for (int pixelIndex = 0; pixelIndex < pixelsCount; pixelIndex++) {
					int x = inputStream.readInt();
					int y = inputStream.readInt();

					pixels.add(new ParticleSpawnPos(x, y, width, height));
				}

				namespacePixels.put(itemPath, pixels);
			}
		}

		return namespacePixels;
	}

	public static void add(Identifier itemId, List<ParticleSpawnPos> pixels) {
		String namespace = itemId.getNamespace();

		synchronized (lock(namespace)) {
			Map<String, List<ParticleSpawnPos>> map = NAMESPACE_PIXELS.computeIfAbsent(
					namespace,
					ignoredNamespace -> new HashMap<>()
			);

			map.put(itemId.getPath(), pixels);
		}
	}

	public static void save(String namespace) {
		Map<String, List<ParticleSpawnPos>> map = NAMESPACE_PIXELS.get(namespace);
		if (map == null || map.isEmpty()) {
			return;
		}

		Map<String, List<ParticleSpawnPos>> snapshot;

		synchronized (lock(namespace)) {
			map = NAMESPACE_PIXELS.get(namespace);
			if (map == null || map.isEmpty()) {
				return;
			}

			snapshot = new HashMap<>(map);
		}

		Path file = FamilyParticlesCacheManager.FOLDER.resolve(namespace).resolve("spawn_areas.cached");
		Path temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");

		Util.ioPool().execute(() -> {
			synchronized (lock(namespace)) {
				try {
					Files.createDirectories(file.getParent());

					try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryFile)))) {
						out.writeInt(MAGIC);
						out.writeInt(VERSION);
						out.writeInt(snapshot.size());

						for (Map.Entry<String, List<ParticleSpawnPos>> entry : snapshot.entrySet()) {
							out.writeUTF(entry.getKey());
							writePixels(out, entry.getValue());
						}
					}

					Files.move(
							temporaryFile,
							file,
							StandardCopyOption.REPLACE_EXISTING,
							StandardCopyOption.ATOMIC_MOVE
					);
				} catch (IOException exception) {
					InventoryParticlesClient.LOGGER.error(
							"Failed to cache pixels for namespace {}, reason:",
							namespace,
							exception
					);

					try {
						Files.deleteIfExists(temporaryFile);
					} catch (IOException ignoredException) {
					}
				}
			}
		});
	}

	private static void writePixels(DataOutputStream out, List<ParticleSpawnPos> pixels) throws IOException {
		out.writeInt(pixels.size());

		if (pixels.isEmpty()) {
			out.writeInt(0);
			out.writeInt(0);
			return;
		}

		ParticleSpawnPos origin = pixels.get(0);

		out.writeInt(origin.width());
		out.writeInt(origin.height());

		for (ParticleSpawnPos pixel : pixels) {
			out.writeInt(pixel.x());
			out.writeInt(pixel.y());
		}
	}
}