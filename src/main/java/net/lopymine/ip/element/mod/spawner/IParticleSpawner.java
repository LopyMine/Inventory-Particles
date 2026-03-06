package net.lopymine.ip.element.mod.spawner;

import java.util.List;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.*;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;

public interface IParticleSpawner {

	List<InventoryParticle> tickAndSpawn(ParticleSpawnContext cursor);

	List<InventoryParticle> spawnFromCursor(InventoryCursor cursor);

	List<InventoryParticle> spawn(ParticleSpawnContext context);

	void bump(ParticleHolder holder);
}
