package net.lopymine.ip.family.atlas.manager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.*;
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

@Getter
@Setter
public class FamilyParticlesAtlasManager {

	private static final Map<String, FamilyParticlesAtlasManager> CREATED_ATLASES = new ConcurrentHashMap<>();

	private final StitchHooksManager stitchHooksManager = new StitchHooksManager();
	private final AtomicInteger latestAtlasVersion = new AtomicInteger();

	@Nullable
	private LockableAtlasTexture atlasTexture;
	private Identifier atlasId;

	public FamilyParticlesAtlasManager(String atlasId) {
		this.atlasId = InventoryParticles.id(atlasId);
	}

	@Nullable
	public static FamilyParticlesAtlasManager get(String id) {
		return CREATED_ATLASES.get(id);
	}

	public static FamilyParticlesAtlasManager getOrCreate(String namespace) {
		return CREATED_ATLASES.computeIfAbsent(namespace, FamilyParticlesAtlasManager::new);
	}

	@NotNull
	public TextureAtlas createNotRegisteredInstance() {
		return new TextureAtlas(this.atlasId);
	}

	@NotNull
	public TextureAtlasSprite getSprite(Identifier id) {
		if (this.atlasTexture == null) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return this.atlasTexture.getAtlas().getSprite(id);
	}

	public void setAtlas(@NotNull TextureAtlas texture) {
		if (this.atlasTexture != null && this.atlasTexture.isLocked()) {
			LockableAtlasTexture atlasTexture = new LockableAtlasTexture(texture);
			this.atlasTexture.setUnlockHook(() -> set(atlasTexture));
			return;
		}

		set(new LockableAtlasTexture(texture));
	}

	@NotNull
	private LockableAtlasTexture set(@NotNull LockableAtlasTexture texture) {
		TextureAtlas atlas = texture.getAtlas();
		this.atlasTexture = texture;
		Identifier id = atlas.location();
		Minecraft.getInstance().getTextureManager().register(id, atlas);
		return this.atlasTexture;
	}

	public void stitchAndUpdate(Set<AtlasSprite> sprites, @Nullable OnAtlasStitched onAtlasStitched) {
		stitchAndUpdate(sprites, Minecraft.getInstance(), onAtlasStitched);
	}

	public void stitchAndUpdate(Set<AtlasSprite> sprites, Executor executor, @Nullable OnAtlasStitched onAtlasStitched) {
		stitchAndUpdate(sprites, null, executor, Minecraft.getInstance(), onAtlasStitched);
	}

	public void stitchAndUpdate(Set<AtlasSprite> sprites, @Nullable PreparableReloadListener.PreparationBarrier synchronizer, Executor prepareExecutor, Executor applyExecutor, @Nullable OnAtlasStitched onAtlasStitched) {
		this.stitchHooksManager.runAllHooks(false);

		int currentId = this.latestAtlasVersion.incrementAndGet();
		this.stitchHooksManager.addHook(onAtlasStitched);

		TextureAtlas atlasTexture = this.createNotRegisteredInstance();

		List<SpriteContents> contents = sprites.stream().map(AtlasSprite::getContents).filter(Objects::nonNull).toList();

		CompletableFuture<Preparations> future = CompletableFuture.supplyAsync(
				() -> SpriteLoader.create(atlasTexture).stitch(contents, 0, prepareExecutor)
		);

		if (synchronizer != null) {
			future = future.thenCompose(synchronizer::wait);
		}

		AtlasStitchingContext stitchingContext = new AtlasStitchingContext(currentId, atlasTexture, sprites, this);
		future.thenAcceptAsync(stitchingContext::upload, applyExecutor);
	}

	public static void closeAll() {
		for (FamilyParticlesAtlasManager manager : CREATED_ATLASES.values()) {
			manager.close();
		}
	}

	public void close() {
		if (this.atlasTexture == null) {
			return;
		}
		this.atlasTexture.getAtlas().close();
	}

	private record AtlasStitchingContext(int version, TextureAtlas atlas, Set<AtlasSprite> atlasSprites, FamilyParticlesAtlasManager manager) {

		public void upload(Preparations result) {
			int latestAtlasVersion = this.manager.getLatestAtlasVersion().get();
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
			this.manager.setAtlas(this.atlas);
			this.manager.getStitchHooksManager().runAllHooks(true);
		}

	}

}
