package net.lopymine.ip.spawner;

import java.util.*;
import net.lopymine.ip.config.particle.ParticleHolder;
import net.lopymine.ip.t2o.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.*;

public record ParticleSpawnArea(IParticleSpawnPos[] positions) implements IParticleSpawnArea {

	@Nullable
	public static ParticleSpawnArea readFromTexture(Identifier id) {
		if (ParticleHolder.STANDARD_SPAWN_AREA.equals(id)) {
			return null;
		}

		List<ParticleSpawnPos> list = Texture2ObjectsManager.readFromTexture(
				id,
				"spawn area",
				Texture2ObjectPixelFilter.NOT_TRANSPARENT,
				(x, y, width, height, color) -> new ParticleSpawnPos(x, y, width, height)
		);
		return new ParticleSpawnArea(list.toArray(IParticleSpawnPos[]::new));
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
