package net.lopymine.ip.family.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.*;
import lombok.*;
import net.lopymine.ip.family.atlas.stitch.OnSpriteUploaded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;

//? if >=1.21.1 {

import net.minecraft.server.packs.resources.ResourceMetadata;

//?}

//? if >=26.1 {
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
//?}

@Setter
@Getter
public class AtlasSprite {

	@NotNull
	private Identifier spriteId;
	@Nullable
	private SpriteContents contents;

	private long cachedId = -1;
	private boolean closable = true;
	@Nullable
	private Runnable unregisterAction;
	private OnSpriteUploaded uploadAction;

	private volatile boolean uploaded;

	public AtlasSprite(@NotNull Identifier spriteId) {
		this.spriteId = spriteId;
	}

	@Nullable
	public static AtlasSprite of(@Nullable Identifier spriteId) {
		if (spriteId == null) {
			return null;
		}
		return new AtlasSprite(spriteId);
	}

	public static AtlasSprite of(@Nullable SpriteContents contents) {
		if (contents == null) {
			return null;
		}
		AtlasSprite atlasSprite = new AtlasSprite(contents.name());
		atlasSprite.setContents(contents);
		return atlasSprite;
	}

	public static AtlasSprite of(Identifier spriteId, NativeImage image) {
		AtlasSprite atlasSprite = new AtlasSprite(spriteId);
		updateContents(atlasSprite, image);
		return atlasSprite;
	}

	public static void updateContents(AtlasSprite sprite, NativeImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		FrameSize dimensions = new FrameSize(width, height);

		//? if >=26.1 {
		var metadata = ResourceMetadata.EMPTY;
		Optional<AnimationMetadataSection> decode = metadata.getSection(AnimationMetadataSection.TYPE);
		Optional<TextureMetadataSection> decode2 = metadata.getSection(TextureMetadataSection.TYPE);

		SpriteContents contents = new SpriteContents(sprite.getSpriteId(), dimensions, image, decode, List.of(), decode2);
		//?} elif >=1.21.10 {
		/*SpriteContents contents = new SpriteContents(sprite.getSpriteId(), dimensions, image);
		*///?} elif >=1.21.1 {
		/*SpriteContents contents = new SpriteContents(sprite.getSpriteId(), dimensions, image, ResourceMetadata.EMPTY);
		*///?} else {
		/*SpriteContents contents = new SpriteContents(sprite.getSpriteId(), dimensions, image, AnimationMetadataSection.EMPTY);
		*///?}

		sprite.setContents(contents);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AtlasSprite that)) return false;
		return Objects.equals(this.getSpriteId(), that.getSpriteId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getSpriteId());
	}

	private boolean cannotClose(OnSpriteUploaded closeOnRegistered) {
		if (!this.uploaded) {
			this.uploadAction = this.uploadAction != null ? this.uploadAction.then(closeOnRegistered) : closeOnRegistered;
			return true;
		}
		return false;
	}

	public void markUploaded() {
		this.uploaded = true;
		if (this.uploadAction != null) {
			this.uploadAction.onUploaded(this);
			this.uploadAction = null;
		}
	}

	public void copyFrom(AtlasSprite registeredSprite) {
		this.closable         = registeredSprite.isClosable();
		this.spriteId         = registeredSprite.getSpriteId();
		this.contents         = registeredSprite.getContents();
		this.unregisterAction = registeredSprite.getUnregisterAction();
		this.uploaded         = registeredSprite.isUploaded();
		this.cachedId         = registeredSprite.getCachedId();
	}
}
