package net.lopymine.ip.element.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.renderer.OptimizedDrawer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
public class AtlasTexture implements ITexture {

	public static final Identifier NO_SPRITE = InventoryParticles.id("no_sprite");

	public static final Codec<AtlasTexture> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			option("sprite", NO_SPRITE, Identifier.CODEC, AtlasTexture::getSpriteNotNull),
			option("atlas", InventoryParticlesAtlasManager.ATLAS_ID, Identifier.CODEC, AtlasTexture::getAtlas)
	).apply(instance, AtlasTexture::new));

	@Nullable
	private Identifier sprite;
	@NotNull
	private Identifier atlas;

	@Nullable
	private TextureAtlasSprite atlasSprite;

	public AtlasTexture(@Nullable TextureAtlasSprite sprite) {
		this(null, null);
		this.atlasSprite = sprite;
	}

	public AtlasTexture(@Nullable Identifier sprite, @Nullable Identifier atlas) {
		this.sprite = sprite;
		this.atlas  = atlas == null ? InventoryParticlesAtlasManager.ATLAS_ID : atlas;
	}

	@Override
	public void render(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		if (this.atlasSprite == null) {
			return;
		}
		OptimizedDrawer.drawParticleSprite(graphics, this.atlasSprite, 0, 0, width, height, color);
	}

	@Override
	public Identifier getId() {
		return this.getSpriteNotNull();
	}

	@Override
	public void initialize() {
		this.atlasSprite = InventoryParticlesAtlasManager.getInstance().getSprite(this.sprite, this.atlas);
	}

	@Override
	public void clear() {
		this.atlasSprite = null;
	}

	@NotNull
	public Identifier getSpriteNotNull() {
		return this.sprite == null ? NO_SPRITE : this.sprite;
	}
}
