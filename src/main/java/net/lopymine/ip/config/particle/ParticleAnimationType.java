package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.*;

public enum ParticleAnimationType implements StringRepresentable {

	STRETCH,
	ONETIME,
	LOOP,
	RANDOM,
	RANDOM_STATIC;

	public static final Codec<ParticleAnimationType> CODEC = StringRepresentable.fromEnum(ParticleAnimationType::values);

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
