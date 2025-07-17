package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import net.minecraft.util.*;

public enum ParticleAnimationType implements StringIdentifiable {

	STRETCH,
	ONETIME,
	LOOP,
	RANDOM,
	RANDOM_STATIC;

	public static final Codec<ParticleAnimationType> CODEC = StringIdentifiable.createCodec(ParticleAnimationType::values);

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
