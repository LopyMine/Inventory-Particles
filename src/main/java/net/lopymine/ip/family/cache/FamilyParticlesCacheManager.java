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

	public static void delete() throws IOException {
		if (!Files.exists(FOLDER)) {
			return;
		}
		try (Stream<Path> stream = Files.walk(FOLDER).sorted(Comparator.reverseOrder())) {
			stream.forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
			throw e;
		}
	}
}