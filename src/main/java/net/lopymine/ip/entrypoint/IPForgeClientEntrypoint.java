package net.lopymine.ip.entrypoint;

//? if forge {

/*import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.resourcepack.InventoryParticlesClientReloadListener;
import net.lopymine.mossylib.modmenu.*;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.lopymine.ip.modmenu.ModMenuIntegration;

public class IPForgeClientEntrypoint {

	public static void onInitializeClient() {
		InventoryParticlesClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getActiveContainer());

		FMLJavaModLoadingContext.get().getModEventBus().<RegisterClientReloadListenersEvent>addListener(event -> {
			event.registerReloadListener(new InventoryParticlesClientReloadListener());
		});
	}

}

*///?}

