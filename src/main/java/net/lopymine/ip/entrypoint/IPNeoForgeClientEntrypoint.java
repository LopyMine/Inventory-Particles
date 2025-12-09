package net.lopymine.ip.entrypoint;

//? if neoforge {
/*import net.lopymine.ip.InventoryParticles;

import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.modmenu.ModMenuIntegration;
import net.lopymine.ip.resourcepack.InventoryParticlesClientReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.*;
import net.neoforged.fml.common.Mod;
//? if >=1.21.4 {
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
//?} else {
/^
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
^/
//?}

@Mod(value = InventoryParticles.MOD_ID, dist = Dist.CLIENT)
public class IPNeoForgeClientEntrypoint {

	public IPNeoForgeClientEntrypoint(IEventBus bus, ModContainer container) {
		InventoryParticlesClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(container);

		//? if >=1.21.4 {
		bus.addListener(AddClientReloadListenersEvent.class, event -> {
			event.addListener(InventoryParticlesClientReloadListener.getId(), new InventoryParticlesClientReloadListener());
		});
		//?} else {
		/^bus.addListener(RegisterClientReloadListenersEvent.class, event -> {
			event.registerReloadListener(new InventoryParticlesClientReloadListener());
		});
		^///?}
	}

}

*///?}

