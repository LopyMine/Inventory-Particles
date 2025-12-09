package net.lopymine.ip.atlas;

import java.util.Set;
import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.SpriteLoader.Preparations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;

public class InventoryParticlesAtlasManager {

	public static final ResourceLocation ATLAS_ID = InventoryParticles.id("textures/atlas/iparticles");
	public static final ResourceLocation FOLDER_ID = InventoryParticles.id("iparticles");
	private static InventoryParticlesAtlasManager INSTANCE;
	private final TextureAtlas atlas;

	public InventoryParticlesAtlasManager() {
		this.atlas = new TextureAtlas(ATLAS_ID);
		Minecraft.getInstance().getTextureManager().register(ATLAS_ID, this.atlas);
	}

	public static InventoryParticlesAtlasManager getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new InventoryParticlesAtlasManager();
		}
		return INSTANCE;
	}

	public void reload(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
		//? if >=1.21.9 {
		SpriteLoader.create(this.atlas)
				.loadAndStitch(resourceManager, FOLDER_ID, 0, prepareExecutor, Set.of())
				.thenCompose(synchronizer::wait)
				.thenAcceptAsync(this.atlas::upload, applyExecutor);
		//?} else {
		/*SpriteLoader.create(this.atlas)
				.loadAndStitch(resourceManager, FOLDER_ID, 0, prepareExecutor)
				.thenCompose(SpriteLoader.Preparations::waitForUpload)
				.thenCompose(synchronizer::wait)
				.thenAcceptAsync(this.atlas::upload, applyExecutor);
		*///?}
	}

	public void close() {
		this.atlas.close();
	}

	public TextureAtlasSprite getSprite(ResourceLocation id) {
		return this.atlas.getSprite(id);
	}

	public TextureAtlasSprite getMissingSprite() {
		return /*? if >=1.21 {*/ this.atlas.missingSprite /*?} else {*/ /*this.getSprite(MissingTextureAtlasSprite.getLocation()) *//*?}*/;
	}
}
