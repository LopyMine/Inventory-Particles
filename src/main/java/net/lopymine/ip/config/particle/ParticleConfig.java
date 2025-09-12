package net.lopymine.ip.config.particle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.element.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ParticleConfig {

	public static final Codec<DynamicParticleSizes> DYNAMIC_PARTICLE_SIZE_CODEC = Codec.either(StaticParticleSize.CODEC, DynamicParticleSizes.CODEC)
			.xmap((either) -> {
				return either.right().orElseGet(() -> either.left().map(DynamicParticleSizes::fromStatic).orElse(null));
			}, Either::right);

	public static final Codec<Identifier> TEXTURES_CODEC = Identifier.CODEC.xmap((id) -> {
		if (id.getPath().endsWith(".png")) {
			String path = id.getPath();
			return Identifier.of(id.getNamespace(), path.substring(0, path.length() - 4));
		}
		return id;
	}, (id) -> id);

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
	private ArrayList<Identifier> textures;
	private HashSet<ParticleHolder> holders;
	private ParticlePhysics physics;

	public InventoryParticle createParticle(ParticleSpawnContext context) {
		return new InventoryParticle(this, context);
	}

}
