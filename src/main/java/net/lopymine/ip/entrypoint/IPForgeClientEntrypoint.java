package net.lopymine.ip.entrypoint;

//? if forge {

/*import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.client.command.InventoryParticlesCommandManager;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ip.resourcepack.reload.InventoryParticlesClientReloadListener;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoadingContext;
import net.lopymine.ip.modmenu.ModMenuIntegration;

public class IPForgeClientEntrypoint {

	public static void onInitializeClient() {
		InventoryParticlesClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(ModLoadingContext.get().getActiveContainer());

		MossyLoader.registerReloadListener(new InventoryParticlesClientReloadListener());
		MossyLoader.registerCommands(InventoryParticlesCommandManager::register);

		MinecraftForge.EVENT_BUS.<LevelJoinEvent>addListener((event) -> {
			ParticlesConfigsManager.updateCombinedMap();
		});
	}

	public static class LevelJoinEvent extends Event { }

}

*///?}

