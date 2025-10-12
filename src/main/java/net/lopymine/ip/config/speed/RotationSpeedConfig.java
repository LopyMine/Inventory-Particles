package net.lopymine.ip.config.speed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.config.range.DoubleRange;
import net.minecraft.util.math.random.Random;
import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class RotationSpeedConfig {

	public static final Codec<RotationSpeedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("impulse", new DoubleRange(), DoubleRange.CODEC, RotationSpeedConfig::getImpulse),
			option("impulse_bidirectional", true, Codec.BOOL, RotationSpeedConfig::isImpulseBidirectional),
			option("acceleration", 0.0D, Codec.DOUBLE, RotationSpeedConfig::getAcceleration),
			option("acceleration_bidirectional", true, Codec.BOOL, RotationSpeedConfig::isAccelerationBidirectional),
			option("max", Double.MAX_VALUE, Codec.DOUBLE, RotationSpeedConfig::getMax),
			option("braking", 0.0D, Codec.DOUBLE, RotationSpeedConfig::getBraking),
			option("turbulence", new DoubleRange(), DoubleRange.CODEC, RotationSpeedConfig::getTurbulence)
	).apply(instance, RotationSpeedConfig::new));

	private DoubleRange impulse;
	private boolean impulseBidirectional;
	private double acceleration;
	private boolean accelerationBidirectional;
	private double max;
	private double braking;
	private DoubleRange turbulence;

	public static RotationSpeedConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public double getImpulseBidirectional(Random random) {
		return this.impulse.getRandom(random) * (this.impulseBidirectional && random.nextBoolean() ? -1 : 1);
	}

	public double getAccelerationBidirectional(Random random) {
		return this.acceleration * (this.accelerationBidirectional && random.nextBoolean() ? -1 : 1);
	}
}
