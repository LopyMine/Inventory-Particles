package net.lopymine.ip.config.speed;

import com.mojang.datafixers.util.Either;
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
public class SpeedConfig {

	public static final Codec<DoubleRange> MAX_CODEC = Codec.either(DoubleRange.CODEC, Codec.DOUBLE)
			.xmap((either) -> either.map((r) -> r, (f) -> new DoubleRange(-f, f)), Either::left);

	public static final Codec<SpeedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("impulse", new DoubleRange(), DoubleRange.CODEC, SpeedConfig::getImpulse),
			option("impulse_bidirectional", true, Codec.BOOL, SpeedConfig::isImpulseBidirectional),
			option("acceleration", 0.0D, Codec.DOUBLE, SpeedConfig::getAcceleration),
			option("acceleration_bidirectional", true, Codec.BOOL, SpeedConfig::isAccelerationBidirectional),
			option("max_acceleration", new DoubleRange(-Double.MAX_VALUE, Double.MAX_VALUE), MAX_CODEC, SpeedConfig::getMaxAcceleration),
			option("max", new DoubleRange(-Double.MAX_VALUE, Double.MAX_VALUE), MAX_CODEC, SpeedConfig::getMax),
			option("braking", 0.0D, Codec.DOUBLE, SpeedConfig::getBraking),
			option("turbulence", new DoubleRange(), DoubleRange.CODEC, SpeedConfig::getTurbulence),
			option("cursor_impulse_inherit_coefficient", 0.0D, Codec.DOUBLE, SpeedConfig::getCursorImpulseInheritCoefficient)
	).apply(instance, SpeedConfig::new));

	private DoubleRange impulse;
	private boolean impulseBidirectional;
	private double acceleration;
	private boolean accelerationBidirectional;
	private DoubleRange maxAcceleration;
	private DoubleRange max;
	private double braking;
	private DoubleRange turbulence;
	private double cursorImpulseInheritCoefficient;

	public static SpeedConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public double getImpulseBidirectional(Random random) {
		return this.impulse.getRandom(random) * (this.impulseBidirectional && random.nextBoolean() ? -1 : 1);
	}

	public double getAccelerationBidirectional(Random random) {
		return this.acceleration * (this.accelerationBidirectional && random.nextBoolean() ? -1 : 1);
	}
}
