package net.lopymine.ip.resourcepack.reload;

import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.ip.family.*;
import net.lopymine.ip.family.cache.FamilyParticlesCacheManager;
import net.lopymine.ip.family.generation.TextureGenerationManager;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ip.element.texture.provider.ITextureProvider;
import net.lopymine.mossylib.reload.AbstractResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.*;

public class InventoryParticlesClientReloadListener extends AbstractResourceReloadListener {

	private boolean first = true;

	@Override
	public String getModId() {
		return InventoryParticles.MOD_ID;
	}

	@Override
	protected void reloadStuff(PreparationBarrier barrier, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
		if (this.first && InventoryParticlesConfig.getInstance().getCacheConfig().getInvalidateMode() == CacheInvalidateMode.AFTER_GAME_LAUNCH) {
			FamilyParticlesCacheManager.invalidateSilence();
		}
		if (!this.first && InventoryParticlesConfig.getInstance().getCacheConfig().getInvalidateMode() == CacheInvalidateMode.AFTER_RESOURCE_RELOADING) {
			FamilyParticlesCacheManager.invalidateSilence();
		}
		this.first = false;

		ITextureProvider.clear(); // clear cached sprites
		TextureGenerationManager.clear(); // clear cache and release images
		FamilyParticlesCacheManager.load(); // load cache if exists
		InventoryParticlesAtlasManager.getInstance().reload(barrier, manager, prepareExecutor, applyExecutor); // reload mod atlas
		ParticlesConfigsManager.getInstance().reload(); // reload configs
		FamilyParticlesConfigManager.getInstance().reload(); // reload family configs
		if (Minecraft.getInstance().level != null) {
			ParticlesConfigsManager.updateCombinedMap(); // final combine
		}
	}
}
