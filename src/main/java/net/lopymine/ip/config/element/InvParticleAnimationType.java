package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import net.minecraft.util.*;

public enum InvParticleAnimationType implements StringIdentifiable {

	STRETCH,
	ONETIME,
	LOOP,
	RANDOM,
	RANDOM_STATIC;

	public static final Codec<InvParticleAnimationType> CODEC = StringIdentifiable.createCodec(InvParticleAnimationType::values);

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
