package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.element.*;
import net.lopymine.ip.utils.CodecUtils;
import net.minecraft.util.Identifier;
import static net.lopymine.ip.utils.CodecUtils.option;

@Setter
@Getter
@AllArgsConstructor
public class InvParticleConfig {

	public static final Codec<InvParticleConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("life_time", 0, Codec.INT, InvParticleConfig::getLifeTimeTicks),
			option("animation_type", InvParticleAnimationType.RANDOM, InvParticleAnimationType.CODEC, InvParticleConfig::getAnimationType),
			option("animation_speed", 1, Codec.INT, InvParticleConfig::getAnimationSpeed),
			option("textures", new ArrayList<>(), Identifier.CODEC, InvParticleConfig::getTextures),
			option("holders", new HashSet<>(), InvParticleHolder.CODEC, InvParticleConfig::getHolders),
			option("physics", InvParticlePhysics.getNewInstance(), InvParticlePhysics.CODEC, InvParticleConfig::getPhysics)
	).apply(instance, InvParticleConfig::new));

	private int lifeTimeTicks;
	private InvParticleAnimationType animationType;
	private int animationSpeed;
	private ArrayList<Identifier> textures;
	private HashSet<InvParticleHolder> holders;
	private InvParticlePhysics physics;

	public static InvParticleConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public InvParticle createParticle(Cursor cursor) {
		return new InvParticle(this, cursor);
	}

}
