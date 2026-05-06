package net.lopymine.ip.family.atlas.manager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.family.atlas.*;
import net.lopymine.ip.family.atlas.stitch.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.texture.SpriteLoader.Preparations;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.*;

public class FamilyParticlesAtlasManager {

	public static final Identifier ATLAS_ID = InventoryParticles.id("family_atlas.png");
	private static final StitchHooksManager STITCH_HOOKS_MANAGER = new StitchHooksManager();
	private static final AtomicInteger LATEST_ATLAS_VERSION = new AtomicInteger();

	@Nullable
	private static LockableAtlasTexture ATLAS_TEXTURE;

	@NotNull
	public static TextureAtlas createNotRegisteredInstance() {
		return new TextureAtlas(ATLAS_ID);
	}

	@NotNull
	public static TextureAtlasSprite getSprite(Identifier id) {
		if (ATLAS_TEXTURE == null) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return ATLAS_TEXTURE.getAtlas().getSprite(id);
	}

	public static void setAtlas(@NotNull TextureAtlas texture) {
		if (ATLAS_TEXTURE != null && ATLAS_TEXTURE.isLocked()) {
			LockableAtlasTexture atlasTexture = new LockableAtlasTexture(texture);
			ATLAS_TEXTURE.setUnlockHook(() -> set(atlasTexture));
			return;
		}


		set(new LockableAtlasTexture(texture));
	}

	@NotNull
	private static LockableAtlasTexture set(@NotNull LockableAtlasTexture texture) {
		TextureAtlas atlas = texture.getAtlas();
		ATLAS_TEXTURE = texture;
		Identifier id = atlas.location();
		Minecraft.getInstance().getTextureManager().register(id, atlas);
		return ATLAS_TEXTURE;
	}

	public static void stitchAndUpdate(Set<AtlasSprite> sprites, @Nullable OnAtlasStitched onAtlasStitched) {
		stitchAndUpdate(sprites, Minecraft.getInstance(), onAtlasStitched);
	}

	public static void stitchAndUpdate(Set<AtlasSprite> sprites, Executor executor, @Nullable OnAtlasStitched onAtlasStitched) {
		stitchAndUpdate(sprites, null, executor, Minecraft.getInstance(), onAtlasStitched);
	}

	public static void stitchAndUpdate(Set<AtlasSprite> sprites, @Nullable PreparableReloadListener.PreparationBarrier synchronizer, Executor prepareExecutor, Executor applyExecutor, @Nullable OnAtlasStitched onAtlasStitched) {
		int currentId = LATEST_ATLAS_VERSION.incrementAndGet();
		STITCH_HOOKS_MANAGER.addHook(onAtlasStitched);

		TextureAtlas atlasTexture = FamilyParticlesAtlasManager.createNotRegisteredInstance();

		List<SpriteContents> contents = sprites.stream().map(AtlasSprite::getContents).filter(Objects::nonNull).toList();

		CompletableFuture<Preparations> future = CompletableFuture.supplyAsync(
				() -> SpriteLoader.create(atlasTexture).stitch(contents, 0, prepareExecutor)
		);

		if (synchronizer != null) {
			future = future.thenCompose(synchronizer::wait);
		}

		AtlasStitchingContext stitchingContext = new AtlasStitchingContext(currentId, atlasTexture, sprites);
		future.thenAcceptAsync(stitchingContext::upload, applyExecutor);
	}

	public static void close() {
		if (ATLAS_TEXTURE == null) {
			return;
		}
		ATLAS_TEXTURE.getAtlas().close();
	}

	private record AtlasStitchingContext(int version, TextureAtlas atlas, Set<AtlasSprite> atlasSprites) {

		public void upload(Preparations result) {
			int latestAtlasVersion = LATEST_ATLAS_VERSION.get();
			if (this.version != latestAtlasVersion) {
				InventoryParticles.LOGGER.warn("Skipped atlas stitching, waiting \"{}\"", latestAtlasVersion);
				return;
			}
			try {
				this.atlas.upload(result);
			} catch (Exception e) {
				InventoryParticles.LOGGER.warn("Failed to upload sprites into atlas:", e);
			}
			this.atlasSprites.forEach(AtlasSprite::markUploaded);
			FamilyParticlesAtlasManager.setAtlas(this.atlas);
			STITCH_HOOKS_MANAGER.runAllHooks();
		}

	}

}
