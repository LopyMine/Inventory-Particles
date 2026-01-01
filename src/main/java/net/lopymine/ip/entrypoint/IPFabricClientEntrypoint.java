package net.lopymine.ip.entrypoint;

//? if fabric {

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.resourcepack.InventoryParticlesClientReloadListener;
import net.minecraft.server.packs.PackType;

//? if >=1.21.9 {

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;

//?}

public class IPFabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		InventoryParticlesClient.onInitializeClient();
		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
			InventoryParticlesAtlasManager.getInstance().close();
		});
		//? if >=1.21.9 {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(InventoryParticlesClientReloadListener.getFabricId(), new InventoryParticlesClientReloadListener());
		//?} else {
		/*ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new InventoryParticlesClientReloadListener());
		*///?}


	}
}

//?}
