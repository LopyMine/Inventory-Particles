package net.lopymine.ip.config.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.element.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.resources.Identifier;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ParticleConfig {

	public static final Codec<DynamicParticleSizes> DYNAMIC_PARTICLE_SIZE_CODEC = Codec.either(StaticParticleSize.CODEC, DynamicParticleSizes.CODEC)
			.xmap((either) -> {
				return either.right().orElseGet(() -> either.left().map(DynamicParticleSizes::fromStatic).orElse(null));
			}, Either::right);

	public static final Codec<Identifier> SPRITE_CODEC = Identifier.CODEC.xmap((id) -> {
		if (id.getPath().endsWith(".png")) {
			String path = id.getPath();
			String i = id.getNamespace();
			String s = path.substring(0, path.length() - 4);
			//? if >=1.21 {
			return Identifier.fromNamespaceAndPath(i, s);
			//?} else {
			/*return Identifier.tryBuild(i, s);
			 *///?}
		}
		return id;
	}, (id) -> id);

	public static final Codec<ParticleTexture> TEXTURES_CODEC = Codec.either(SPRITE_CODEC, ParticleTexture.CODEC).xmap((either) -> {
		Optional<Identifier> left = either.left();
		return left.map((id) -> new ParticleTexture(id, InventoryParticlesAtlasManager.ATLAS_ID))
				.orElseGet(() -> either.right().orElseThrow());
	}, (particleTexture) -> {
		if (particleTexture.getAtlasId() == InventoryParticlesAtlasManager.ATLAS_ID) {
			return Either.left(particleTexture.getSpriteNotNull());
		}
		return Either.right(particleTexture);
	});

	public static final Codec<ParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("life_time", 0, Codec.INT, ParticleConfig::getLifeTimeTicks),
			option("animation_type", ParticleAnimationType.RANDOM, ParticleAnimationType.CODEC, ParticleConfig::getAnimationType),
			option("animation_speed", 1.0D, Codec.DOUBLE, ParticleConfig::getAnimationSpeed),
			option("size", DynamicParticleSizes.STANDARD, DYNAMIC_PARTICLE_SIZE_CODEC, ParticleConfig::getSize),
			option("textures", new ArrayList<>(), TEXTURES_CODEC, ParticleConfig::getTextures),
			option("holders", new HashSet<>(), ParticleHolder.CODEC, ParticleConfig::getHolders),
			option("physics", ParticlePhysics.getNewInstance(), ParticlePhysics.CODEC, ParticleConfig::getPhysics)
	).apply(instance, ParticleConfig::new));

	private int lifeTimeTicks;
	private ParticleAnimationType animationType;
	private double animationSpeed;
	private DynamicParticleSizes size;
	private ArrayList<ParticleTexture> textures;
	private HashSet<ParticleHolder> holders;
	private ParticlePhysics physics;

	public InventoryParticle createParticle(ParticleSpawnContext context) {
		return new InventoryParticle(this, context);

	}

}
