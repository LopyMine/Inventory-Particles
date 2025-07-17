package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.element.*;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class ParticleConfig {

	public static final Codec<ParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("life_time", 0, Codec.INT, ParticleConfig::getLifeTimeTicks),
			option("animation_type", ParticleAnimationType.RANDOM, ParticleAnimationType.CODEC, ParticleConfig::getAnimationType),
			option("animation_speed", 1.0F, Codec.FLOAT, ParticleConfig::getAnimationSpeed),
			option("textures", new ArrayList<>(), Identifier.CODEC, ParticleConfig::getTextures),
			option("holders", new HashSet<>(), ParticleHolder.CODEC, ParticleConfig::getHolders),
			option("physics", ParticlePhysics.getNewInstance(), ParticlePhysics.CODEC, ParticleConfig::getPhysics)
	).apply(instance, ParticleConfig::new));

	private int lifeTimeTicks;
	private ParticleAnimationType animationType;
	private float animationSpeed;
	private ArrayList<Identifier> textures;
	private HashSet<ParticleHolder> holders;
	private ParticlePhysics physics;

	public InventoryParticle createParticle(InventoryCursor cursor) {
		return new InventoryParticle(this, cursor);
	}

}
