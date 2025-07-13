package net.lopymine.ip.spawner;

import java.util.List;
import net.lopymine.ip.element.*;
import net.minecraft.util.math.random.Random;

public interface IParticleSpawner {

	List<InvParticle> tickAndSpawn(Random random, Cursor cursor);

	List<InvParticle> spawnFromSpeed(Random random, Cursor cursor);

//	List<InvParticle> spawnFromSpeedDelta(Random random, Cursor cursor);

}
