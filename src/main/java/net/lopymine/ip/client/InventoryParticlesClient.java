package net.lopymine.ip.client;

import net.lopymine.ip.client.renderer.*;
import net.lopymine.mossylib.logger.MossyLogger;
import org.slf4j.*;

import net.lopymine.ip.InventoryParticles;

public class InventoryParticlesClient {

	public static MossyLogger LOGGER = InventoryParticles.LOGGER.extend("Client");

	public final static DebugParticleInfoRenderer DEBUG_PARTICLE_INFO_RENDERER = new DebugParticleInfoRenderer();
	public final static DebugCursorInfoRenderer DEBUG_CURSOR_INFO_RENDERER = new DebugCursorInfoRenderer();

	public static void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryParticles.MOD_NAME);

	}
}
