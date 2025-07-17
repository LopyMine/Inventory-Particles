package net.lopymine.ip.spawner;

import java.util.List;
import net.lopymine.ip.element.*;

public interface IParticleSpawner {

	List<InventoryParticle> tickAndSpawn(InventoryCursor cursor);

	List<InventoryParticle> spawnFromSpeed(InventoryCursor cursor);

}
