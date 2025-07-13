package net.lopymine.ip.client;

import lombok.*;
import net.lopymine.ip.client.renderer.*;
import net.lopymine.ip.manager.*;
import org.slf4j.*;

import net.fabricmc.api.ClientModInitializer;

import net.lopymine.ip.InventoryParticles;

public class InventoryParticlesClient implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Client");

	@Getter
	private final static InvParticleDebugInfoRenderer particleRenderer = new InvParticleDebugInfoRenderer();
	@Getter
	private final static CursorDebugInfoRenderer cursorRenderer = new CursorDebugInfoRenderer();

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryParticles.MOD_NAME);
		InvParticlesClientReloadListener.register();
	}
}
