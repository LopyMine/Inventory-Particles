package net.lopymine.ip.entrypoint;

//? if neoforge {
/*import net.lopymine.ip.InventoryParticles;

import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.client.command.InventoryParticlesCommandManager;
import net.lopymine.ip.modmenu.ModMenuIntegration;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ip.resourcepack.reload.InventoryParticlesClientReloadListener;
import net.lopymine.mossylib.loader.MossyLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod(value = InventoryParticles.MOD_ID, dist = Dist.CLIENT)
public class IPNeoForgeClientEntrypoint {

	public IPNeoForgeClientEntrypoint(IEventBus bus, ModContainer container) {
		InventoryParticlesClient.onInitializeClient();
		ModMenuIntegration integration = new ModMenuIntegration();
		integration.register(container);

		MossyLoader.registerReloadListener(new InventoryParticlesClientReloadListener());
		MossyLoader.registerCommands(InventoryParticlesCommandManager::register);

		NeoForge.EVENT_BUS.addListener(LevelJoinEvent.class, (event) -> {
			ParticlesConfigsManager.updateCombinedMap();
		});

	}

	public static class LevelJoinEvent extends Event { }

}

*///?}

