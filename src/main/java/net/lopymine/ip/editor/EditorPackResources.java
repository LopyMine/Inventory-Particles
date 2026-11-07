package net.lopymine.ip.editor;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.element.texture.provider.ITextureProvider;
import net.lopymine.ip.t2o.Texture2ObjectsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class EditorPackResources implements InventoryParticlesAtlasManager.SpriteOverride {

	public static final Identifier ATLAS_ID = InventoryParticles.id("textures/atlas/editor_particles");

	private final PackEditSession session;
	private final TextureAtlas atlas;

	@Nullable
	private CloseableResourceManager manager;

	private boolean ready;
	private boolean closed;

	public EditorPackResources(PackEditSession session) {
		this.session = session;
		this.atlas = new TextureAtlas(ATLAS_ID);

		Minecraft.getInstance().getTextureManager().register(ATLAS_ID, this.atlas);
		InventoryParticlesAtlasManager.setSPRITE_OVERRIDE(this);

		this.reload();
	}

	public void reload() {
		if (this.closed) {
			return;
		}

		this.ready = false;

		CloseableResourceManager previous = this.manager;
		CloseableResourceManager manager = this.createManager();

		this.manager = manager;
		Texture2ObjectsManager.setRESOURCE_MANAGER_OVERRIDE(manager);
		ITextureProvider.clear();

		SpriteLoader.create(this.atlas)
				//? if >=1.21.9 {
				.loadAndStitch(manager, InventoryParticlesAtlasManager.FOLDER_ID, 0, Util.backgroundExecutor(), Set.of())
				//?} else {
				/*.loadAndStitch(manager, InventoryParticlesAtlasManager.FOLDER_ID, 0, Util.backgroundExecutor())
				*///?}
				.whenCompleteAsync((preparations, throwable) -> this.upload(preparations, throwable, previous), Minecraft.getInstance());
	}

	private CloseableResourceManager createManager() {
		List<PackResources> packs = new ArrayList<>(Minecraft.getInstance().getResourcePackRepository().openAllSelected());
		packs.add(this.createWorkFolderPack());
		return new MultiPackResourceManager(PackType.CLIENT_RESOURCES, packs);
	}

	private PackResources createWorkFolderPack() {
		String packName = this.session.getPackName();
		String packId = "%s_editor/%s".formatted(InventoryParticles.MOD_ID, packName);

		//? if >=1.21.1 {
		PackLocationInfo location = new PackLocationInfo(packId, Component.literal(packName), PackSource.BUILT_IN, Optional.empty());
		return new PathPackResources(location, this.session.getWorkFolder());
		//?} else {
		/*return new PathPackResources(packId, this.session.getWorkFolder(), true);
		*///?}
	}

	private void upload(@Nullable SpriteLoader.Preparations preparations, @Nullable Throwable throwable, @Nullable CloseableResourceManager previous) {
		if (previous != null) {
			previous.close();
		}
		if (throwable != null || preparations == null) {
			InventoryParticlesClient.LOGGER.error("Failed to stitch editor atlas of resource pack \"{}\":", this.session.getPackName(), throwable);
			return;
		}
		if (this.closed) {
			return;
		}

		this.atlas.upload(preparations);
		this.ready = true;
		ITextureProvider.clear();
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(Identifier id) {
		if (!this.ready || this.closed) {
			return null;
		}

		TextureAtlasSprite sprite = this.atlas.getSprite(id);
		return MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name()) ? null : sprite;
	}

	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.ready = false;

		InventoryParticlesAtlasManager.setSPRITE_OVERRIDE(null);
		Texture2ObjectsManager.setRESOURCE_MANAGER_OVERRIDE(null);
		Minecraft.getInstance().getTextureManager().release(ATLAS_ID);
		ITextureProvider.clear();

		if (this.manager != null) {
			this.manager.close();
			this.manager = null;
		}
	}
}
