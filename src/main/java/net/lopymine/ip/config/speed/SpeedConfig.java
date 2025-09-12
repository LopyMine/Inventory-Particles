package net.lopymine.ip.config.speed;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.config.range.FloatRange;
import net.lopymine.ip.utils.CodecUtils;
import net.minecraft.util.math.random.Random;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class SpeedConfig {

	public static final Codec<FloatRange> MAX_CODEC = Codec.either(FloatRange.CODEC, Codec.FLOAT)
			.xmap((either) -> either.map((r) -> r, (f) -> new FloatRange(-f, f)), Either::left);

	public static final Codec<SpeedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("impulse", new FloatRange(), FloatRange.CODEC, SpeedConfig::getImpulse),
			option("impulse_bidirectional", true, Codec.BOOL, SpeedConfig::isImpulseBidirectional),
			option("acceleration", 0.0F, Codec.FLOAT, SpeedConfig::getAcceleration),
			option("acceleration_bidirectional", true, Codec.BOOL, SpeedConfig::isAccelerationBidirectional),
			option("max_acceleration", new FloatRange(-Float.MAX_VALUE, Float.MAX_VALUE), MAX_CODEC, SpeedConfig::getMaxAcceleration),
			option("max", new FloatRange(-Float.MAX_VALUE, Float.MAX_VALUE), MAX_CODEC, SpeedConfig::getMax),
			option("braking", 0.0F, Codec.FLOAT, SpeedConfig::getBraking),
			option("turbulence", new FloatRange(), FloatRange.CODEC, SpeedConfig::getTurbulence),
			option("cursor_impulse_inherit_coefficient", 0.0F, Codec.FLOAT, SpeedConfig::getCursorImpulseInheritCoefficient)
	).apply(instance, SpeedConfig::new));

	private FloatRange impulse;
	private boolean impulseBidirectional;
	private float acceleration;
	private boolean accelerationBidirectional;
	private FloatRange maxAcceleration;
	private FloatRange max;
	private float braking;
	private FloatRange turbulence;
	private float cursorImpulseInheritCoefficient;

	public static SpeedConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public float getImpulseBidirectional(Random random) {
		return this.impulse.getRandom(random) * (this.impulseBidirectional && random.nextBoolean() ? -1 : 1);
	}

	public float getAccelerationBidirectional(Random random) {
		return this.acceleration * (this.accelerationBidirectional && random.nextBoolean() ? -1 : 1);
	}
}
