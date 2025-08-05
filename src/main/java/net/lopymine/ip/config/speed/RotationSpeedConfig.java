package net.lopymine.ip.config.speed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.config.range.FloatRange;
import net.lopymine.ip.utils.CodecUtils;
import net.minecraft.util.math.random.Random;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class RotationSpeedConfig {

	public static final Codec<RotationSpeedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("impulse", new FloatRange(), FloatRange.CODEC, RotationSpeedConfig::getImpulse),
			option("impulse_bidirectional", true, Codec.BOOL, RotationSpeedConfig::isImpulseBidirectional),
			option("acceleration", 0.0F, Codec.FLOAT, RotationSpeedConfig::getAcceleration),
			option("acceleration_bidirectional", true, Codec.BOOL, RotationSpeedConfig::isAccelerationBidirectional),
			option("max", Float.MAX_VALUE, Codec.FLOAT, RotationSpeedConfig::getMax),
			option("braking", 0.0F, Codec.FLOAT, RotationSpeedConfig::getBraking),
			option("turbulence", new FloatRange(), FloatRange.CODEC, RotationSpeedConfig::getTurbulence)
	).apply(instance, RotationSpeedConfig::new));

	private FloatRange impulse;
	private boolean impulseBidirectional;
	private float acceleration;
	private boolean accelerationBidirectional;
	private float max;
	private float braking;
	private FloatRange turbulence;

	public static RotationSpeedConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public float getImpulseBidirectional(Random random) {
		return this.impulse.getRandom(random) * (this.impulseBidirectional && random.nextBoolean() ? -1 : 1);
	}

	public float getAccelerationBidirectional(Random random) {
		return this.acceleration * (this.accelerationBidirectional && random.nextBoolean() ? -1 : 1);
	}
}
