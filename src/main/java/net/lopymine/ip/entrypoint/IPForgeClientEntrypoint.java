package net.lopymine.ip.entrypoint;

//? if forge {

/*import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.client.command.InventoryParticlesCommandManager;
import net.lopymine.ip.particles.ParticlesConfigsManager;
import net.lopymine.ip.resourcepack.InventoryParticlesClientReloadListener;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.modmenu.*;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.lopymine.ip.modmenu.ModMenuIntegration;

public class IPForgeClientEntrypoint {

	public static void onInitializeClient() {
		InventoryParticlesClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getActiveContainer());

		MossyLoader.registerReloadListener(new InventoryParticlesClientReloadListener());
		MossyLoader.registerCommands(InventoryParticlesCommandManager::register);

		MinecraftForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener((event) -> {
			ParticlesConfigsManager.updateCombinedMap();
		});
	}

}

*///?}

