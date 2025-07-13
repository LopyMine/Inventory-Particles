package net.lopymine.ip.spawner;

import java.io.*;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.element.InvParticleHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public record ParticleSpawnArea(IParticleSpawnPos[] positions) implements IParticleSpawnArea {

	@Nullable
	public static ParticleSpawnArea readFromTexture(Identifier id) {
		if (InvParticleHolder.STANDARD_SPAWN_AREA.equals(id)) {
			return new ParticleSpawnArea(new IParticleSpawnPos[0]);
		}
		try {
			Optional<Resource> optional = MinecraftClient.getInstance().getResourceManager().getResource(id);
			if (optional.isEmpty()) {
				return null;
			}
			Resource resource = optional.get();
			InputStream inputStream = resource.getInputStream();
			NativeImage image = NativeImage.read(inputStream);

			List<IParticleSpawnPos> list = new ArrayList<>();

			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getWidth(); y++) {
					int alpha = ColorHelper.getAlpha(image.getColorArgb(x, y));
					if (alpha == 0) {
						continue;
					}
					list.add(new ParticleSpawnPos(x, y));
				}
			}

			return new ParticleSpawnArea(list.toArray(IParticleSpawnPos[]::new));
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to load particle spawn area from \"{}\"! Reason:", id, e);
		}
		return null;
	}

	@Override
	@Nullable
	public IParticleSpawnPos getRandomPos(Random random) {
		if (this.positions.length == 0) {
			return null;
		}
		return this.positions[random.nextBetween(0, this.positions.length-1)];
	}
}
