package net.lopymine.ip.resourcepack;

import java.util.concurrent.Executor;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.resourcepack.particles.ParticlesConfigsManager;
import net.lopymine.ip.texture.IInventoryElementTextureProvider;
import net.lopymine.mossylib.reload.AbstractResourceReloadListener;
import net.minecraft.server.packs.resources.*;

public class InventoryParticlesClientReloadListener extends AbstractResourceReloadListener {

	@Override
	public String getModId() {
		return InventoryParticles.MOD_ID;
	}

	@Override
	protected void reloadStuff(PreparationBarrier barrier, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		IInventoryElementTextureProvider.clear();
		InventoryParticlesAtlasManager.getInstance().reload(barrier, manager, prepareExecutor, applyExecutor);
		ParticlesConfigsManager.getInstance().reload();
	}
}
