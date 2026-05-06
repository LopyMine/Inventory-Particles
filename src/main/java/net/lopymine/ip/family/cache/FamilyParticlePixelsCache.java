package net.lopymine.ip.family.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.element.mod.spawner.ParticleSpawnPos;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class FamilyParticlePixelsCache {

	private static final int MAGIC = 0x49505058; // "IPPX"
	private static final int VERSION = 1;

	private FamilyParticlePixelsCache() {}

	private static Path toFilePath(Identifier id) {
		return FamilyParticlesCacheManager.FOLDER
				.resolve(id.getNamespace())
				.resolve(id.getPath() + ".cached");
	}

	@Nullable
	public static List<ParticleSpawnPos> load(Identifier itemId) {
		Path file = toFilePath(itemId);

		if (!Files.isRegularFile(file)) {
			return null;
		}

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
			int magic = in.readInt();
			if (magic != MAGIC) {
				return null;
			}

			int version = in.readInt();
			if (version != VERSION) {
				return null;
			}

			int count = in.readInt();
			if (count < 0) {
				return null;
			}

			int width = in.readInt();
			int height = in.readInt();

			List<ParticleSpawnPos> pixels = new ArrayList<>(count);

			for (int i = 0; i < count; i++) {
				int x = in.readInt();
				int y = in.readInt();

				pixels.add(new ParticleSpawnPos(x, y, width, height));
			}

			return pixels;
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error(
					"Failed to load cached pixels for item {}, reason:",
					itemId,
					e
			);
			return null;
		}
	}

	public static void save(Identifier itemId, List<ParticleSpawnPos> pixels) {
		Path file = toFilePath(itemId);

		Util.ioPool().execute(() -> {
			try {
				Files.createDirectories(file.getParent());

				try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
					out.writeInt(MAGIC);
					out.writeInt(VERSION);
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
			} catch (IOException e) {
				InventoryParticlesClient.LOGGER.error(
						"Failed to cache pixels for item with id {}, reason:",
						itemId,
						e
				);
			}
		});
	}
}