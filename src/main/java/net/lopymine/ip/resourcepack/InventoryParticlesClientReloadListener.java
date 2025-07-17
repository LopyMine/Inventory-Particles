package net.lopymine.ip.resourcepack;

import net.fabricmc.fabric.api.resource.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

public class InventoryParticlesClientReloadListener implements SimpleSynchronousResourceReloadListener {

	public static void register() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new InventoryParticlesClientReloadListener());
	}

	@Override
	public Identifier getFabricId() {
		return InventoryParticles.id("%s-reload-listener".formatted(InventoryParticles.MOD_ID));
	}

	@Override
	public void reload(ResourceManager manager) {
		ResourcePackParticleConfigsManager.reload();
	}
}
