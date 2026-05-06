package net.lopymine.ip.family.cache;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;

public class FamilyParticlesCacheManager {

	private static CompletableFuture<Object> INVALIDATING_FUTURE = CompletableFuture.completedFuture(new Object());

	public static final Path FOLDER = MossyLoader.getConfigDir()
			.resolve(InventoryParticles.MOD_ID.replace("_", "-"))
			.resolve("cache");

	private static Path toFilePath(Identifier id) {
		return FamilyParticlesCacheManager.FOLDER
				.resolve(id.getNamespace())
				.resolve(id.getPath() + ".png");
	}
	private static Identifier fromFilePath(Path file) {
		Path relative = FOLDER.relativize(file);
		if (relative.getNameCount() < 2) {
			return null;
		}

		String namespace = relative.getName(0).toString();

		String path = relative
				.subpath(1, relative.getNameCount())
				.toString()
				.replace('\\', '/');

		if (!path.endsWith(".png")) {
			return null;
		}

		path = path.substring(0, path.length() - ".png".length());

		try {
			return InventoryParticles.parseId(namespace + ":" + path);
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to parse id from file name {}", file, e);
			return null;
		}
	}

	public static void invalidateSilenceAsync() {
		if (!INVALIDATING_FUTURE.isDone()) {
			return;
		}
		INVALIDATING_FUTURE = CompletableFuture.supplyAsync(() -> {
			try {
				invalidate();
			} catch (IOException e) {
				InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
			}
			return new Object();
		});
	}

	public static void invalidateSilence() {
		try {
			invalidate();
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to delete cache folder:", e);
		}
	}

	public static void invalidate() throws IOException {
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

	public static void save(Identifier id, NativeImage image) {
		Path file = toFilePath(id);
		Util.ioPool().execute(() -> {
			try {
				Files.createDirectories(file.getParent());
				image.writeToFile(file);
			} catch (IOException e) {
				InventoryParticlesClient.LOGGER.error("Failed to cache particle with id {}, reason:", id, e);
			}
		});
	}

	public static void load() {
		try {
			if (!Files.exists(FOLDER)) {
				return;
			}

			Map<Identifier, NativeImage> map = new HashMap<>();

			try (Stream<Path> paths = Files.walk(FOLDER)) {
				paths.filter(Files::isRegularFile)
						.filter(path -> path.getFileName().toString().endsWith(".png"))
						.forEach(path -> {
							Identifier id = fromFilePath(path);
							if (id == null) {
								return;
							}

							try (InputStream inputStream = Files.newInputStream(path)) {
								NativeImage image = NativeImage.read(inputStream);
								map.put(id, image);
							} catch (IOException e) {
								InventoryParticlesClient.LOGGER.error(
										"Failed to load cached particle with id {}, reason:",
										id,
										e
								);
							}
						});
			}

			TextureGenerationManager.load(map);
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to load cache, reason:", e);
		}
	}
}