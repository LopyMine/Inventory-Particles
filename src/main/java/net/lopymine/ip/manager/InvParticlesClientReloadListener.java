package net.lopymine.ip.manager;

import java.util.*;
import net.fabricmc.fabric.api.resource.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

public class InvParticlesClientReloadListener implements SimpleSynchronousResourceReloadListener {

	public static void register() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new InvParticlesClientReloadListener());
	}

	@Override
	public Identifier getFabricId() {
		return InventoryParticles.id("blabbla_lisntere");
	}

	@Override
	public void reload(ResourceManager manager) {
		InvParticleConfigManager.register();
	}
}
