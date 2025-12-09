package net.lopymine.ip.entrypoint;

//? if fabric {

import net.fabricmc.api.ModInitializer;
import net.lopymine.ip.InventoryParticles;

public class IPFabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		InventoryParticles.onInitialize();
	}
}

//?}
