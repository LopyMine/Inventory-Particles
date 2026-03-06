package net.lopymine.ip.config.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.element.mod.InventoryParticle;
import net.lopymine.ip.element.size.*;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.minecraft.resources.Identifier;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ParticleConfig {

	public static final Codec<DynamicSizesWithInterpolation> DYNAMIC_PARTICLE_SIZE_CODEC = Codec.either(StaticSize.CODEC, DynamicSizesWithInterpolation.CODEC)
			.xmap((either) -> {
				return either.right().orElseGet(() -> either.left().map(DynamicSizesWithInterpolation::fromStatic).orElse(null));
			}, Either::right);

	public static final Codec<ITexture> TEXTURE_OR_ITEM_CODEC = Identifier.CODEC.xmap((id) -> {
		if (id.getPath().endsWith(".png")) {
			String path = id.getPath();
			String s = path.substring(0, path.length() - 4);
			return new AtlasTexture(id.withPath(s), null);
		}
		return new ItemTexture(new CachedItem(id));
	}, ITexture::getId);

	public static final Codec<ITexture> ADVANCED_TEXTURES_CODEC = Codec.either(TEXTURE_OR_ITEM_CODEC, AtlasTexture.CODEC).xmap((either) -> {
		if (either.right().isPresent()) {
			return either.right().get();
		}
		if (either.left().isPresent()) {
			return either.left().get();
		}
		return null;
	}, (texture) -> {
		if (texture instanceof AtlasTexture elementTexture && elementTexture.getAtlas() != InventoryParticlesAtlasManager.ATLAS_ID) {
			return Either.right(elementTexture);
		}
		return Either.left(texture);
	});

	public static final Codec<ParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("life_time", 0, Codec.INT, ParticleConfig::getLifeTimeTicks),
			option("animation_type", TextureAnimationType.RANDOM, TextureAnimationType.CODEC, ParticleConfig::getAnimationType),
			option("animation_speed", 1.0D, Codec.DOUBLE, ParticleConfig::getAnimationSpeed),
			option("size", DynamicSizesWithInterpolation.STANDARD, DYNAMIC_PARTICLE_SIZE_CODEC, ParticleConfig::getSize),
			option("textures", new ArrayList<>(), ADVANCED_TEXTURES_CODEC, ParticleConfig::getTextures),
			option("holders", new HashSet<>(), ParticleHolder.CODEC, ParticleConfig::getHolders),
			option("physics", ParticlePhysics.getNewInstance(), ParticlePhysics.CODEC, ParticleConfig::getPhysics)
	).apply(instance, ParticleConfig::new));

	private int lifeTimeTicks;
	private TextureAnimationType animationType;
	private double animationSpeed;
	private DynamicSizesWithInterpolation size;
	private ArrayList<ITexture> textures;
	private HashSet<ParticleHolder> holders;
	private ParticlePhysics physics;

	public InventoryParticle createParticle(ParticleSpawnContext context) {
		return InventoryParticle.create(this, context);
	}

}
