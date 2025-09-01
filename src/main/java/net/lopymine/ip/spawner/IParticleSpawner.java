package net.lopymine.ip.spawner;

import java.util.List;
import net.lopymine.ip.element.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;

public interface IParticleSpawner {

	List<InventoryParticle> tickAndSpawn(ParticleSpawnContext cursor);

	List<InventoryParticle> spawnFromCursor(InventoryCursor cursor);

	List<InventoryParticle> spawn(ParticleSpawnContext context);

}
