package net.lopymine.ip.spawner;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public interface IParticleSpawnArea {

	@Nullable IParticleSpawnPos getRandomPos(RandomSource random);

}
