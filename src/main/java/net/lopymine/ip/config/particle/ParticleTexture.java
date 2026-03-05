package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
public class ParticleTexture {

	public static final Identifier NO_SPRITE = InventoryParticles.id("no_sprite");

	public static final Codec<ParticleTexture> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			option("sprite", NO_SPRITE, Identifier.CODEC, ParticleTexture::getSpriteNotNull),
			option("atlas", InventoryParticlesAtlasManager.ATLAS_ID, Identifier.CODEC, ParticleTexture::getAtlasId)
	).apply(instance, ParticleTexture::new));

	@Nullable
	private Identifier sprite;
	private Identifier atlasId;

	public ParticleTexture(Identifier sprite, Identifier atlasId) {
		this.sprite  = sprite == NO_SPRITE ? null : sprite;
		this.atlasId = atlasId;
	}

	public Identifier getSpriteOr(Identifier missingSprite) {
		return this.sprite == null ? missingSprite : this.sprite;
	}

	@NotNull
	public Identifier getSpriteNotNull() {
		return this.sprite == null ? NO_SPRITE : this.sprite;
	}
}
