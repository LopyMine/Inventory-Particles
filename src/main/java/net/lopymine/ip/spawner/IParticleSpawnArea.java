package net.lopymine.ip.spawner;

import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface IParticleSpawnArea {

	@Nullable IParticleSpawnPos getRandomPos(Random random);

}
