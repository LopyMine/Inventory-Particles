package net.lopymine.ip.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.renderer.*;
import net.lopymine.ip.resourcepack.InventoryParticlesClientReloadListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;
import org.slf4j.*;

import net.fabricmc.api.ClientModInitializer;

import net.lopymine.ip.InventoryParticles;

public class InventoryParticlesClient implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Client");

	public static final boolean IS_MAC = Util.getOperatingSystem() == OperatingSystem.OSX;

	public final static DebugParticleInfoRenderer DEBUG_PARTICLE_INFO_RENDERER = new DebugParticleInfoRenderer();
	public final static DebugCursorInfoRenderer DEBUG_CURSOR_INFO_RENDERER = new DebugCursorInfoRenderer();

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryParticles.MOD_NAME);
		InventoryParticlesClientReloadListener.register();
		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
			InventoryParticlesAtlasManager.getInstance().close();
		});
	}
}
