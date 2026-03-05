package net.lopymine.ip.entrypoint;

//? if fabric {

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.client.command.InventoryParticlesCommandManager;
import net.lopymine.ip.resourcepack.particles.ParticlesConfigsManager;
import net.lopymine.ip.resourcepack.*;
import net.lopymine.mossylib.loader.MossyLoader;

//? if >=1.21.9 {


//?}

public class IPFabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		InventoryParticlesClient.onInitializeClient();
		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
			InventoryParticlesAtlasManager.getInstance().close();
		});

		MossyLoader.registerReloadListener(new InventoryParticlesClientReloadListener());
		MossyLoader.registerCommands(InventoryParticlesCommandManager::register);

		ClientPlayConnectionEvents.JOIN.register((aa, bb, vv) -> {
			ParticlesConfigsManager.updateCombinedMap();
		});

	}
}

//?}
