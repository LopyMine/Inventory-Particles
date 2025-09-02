package net.lopymine.ip.atlas;

import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

public class InventoryParticlesAtlasManager {

	public static final Identifier ATLAS_ID = InventoryParticles.id("textures/atlas/iparticles");
	public static final Identifier FOLDER_ID = InventoryParticles.id("iparticles");
	private static InventoryParticlesAtlasManager INSTANCE;
	private final SpriteAtlasTexture atlas;

	public InventoryParticlesAtlasManager() {
		this.atlas = new SpriteAtlasTexture(ATLAS_ID);
		MinecraftClient.getInstance().getTextureManager().registerTexture(ATLAS_ID, this.atlas);
	}

	public static InventoryParticlesAtlasManager getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new InventoryParticlesAtlasManager();
		}
		return INSTANCE;
	}

	public void reload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
		SpriteLoader.fromAtlas(this.atlas)
				.load(resourceManager, FOLDER_ID, 0, prepareExecutor)
				.thenCompose(SpriteLoader.StitchResult::whenComplete)
				.thenCompose(synchronizer::whenPrepared)
				.thenAcceptAsync(this.atlas::upload, applyExecutor);
	}

	public void close() {
		this.atlas.close();
	}

	public Sprite getSprite(Identifier id) {
		return this.atlas.getSprite(id);
	}

	public Sprite getMissingSprite() {
		//? if >=1.21.5 {
		/*return this.atlas.missingSprite;
		*//*?} else {*/
		return null;
		/*?}*/
	}
}
