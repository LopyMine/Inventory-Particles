package net.lopymine.ip.family.cache;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.mossylib.loader.MossyLoader;

@ExtensionMethod(NativeImageExtension.class)
public class FamilyParticlesCacheManager {

	public static boolean test = false;

	public static final Path FOLDER = MossyLoader.getConfigDir()
			.resolve(InventoryParticles.MOD_ID.replace("_", "-"))
			.resolve("cache");

	public static void deleteSilence() {
		try {
			delete();
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
		}
	}

	public static void delete230() throws IOException {
		test = true;
		if (!Files.exists(FOLDER)) {
			return;
		}

		Set<String> keepFiles = Set.of(
				"atlas.png",
				"regions.txt",
				"spawn_areas.cached"
		);

		try (Stream<Path> stream = Files.walk(FOLDER).sorted(Comparator.reverseOrder())) {
			stream.forEach(path -> {
				try {
					if (Files.isRegularFile(path)) {
						if (!keepFiles.contains(path.getFileName().toString())) {
							Files.delete(path);
						}
						return;
					}

					if (Files.isDirectory(path) && !path.equals(FOLDER)) {
						try (Stream<Path> children = Files.list(path)) {
							if (children.findAny().isEmpty()) {
								Files.delete(path);
							}
						}
					}
				} catch (IOException e) {
					InventoryParticlesClient.LOGGER.error("Failed to delete specific cache path {}:", path, e);
				}
			});
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
			throw e;
		}
		test = false;
	}

	public static void delete() throws IOException {
		if (!Files.exists(FOLDER)) {
			return;
		}
		try (Stream<Path> stream = Files.walk(FOLDER).sorted(Comparator.reverseOrder())) {
			stream.forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					InventoryParticlesClient.LOGGER.error("Failed to delete specific cache path {}:", path, e);
				}
			});
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
			throw e;
		}
	}
}