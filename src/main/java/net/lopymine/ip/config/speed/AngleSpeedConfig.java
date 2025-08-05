package net.lopymine.ip.config.speed;

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
public class AngleSpeedConfig {

	public static final Codec<AngleSpeedConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("impulse", new FloatRange(), FloatRange.CODEC, AngleSpeedConfig::getImpulse),
			option("impulse_bidirectional", true, Codec.BOOL, AngleSpeedConfig::isImpulseBidirectional),
			option("acceleration", 0.0F, Codec.FLOAT, AngleSpeedConfig::getAcceleration),
			option("acceleration_bidirectional", true, Codec.BOOL, AngleSpeedConfig::isAccelerationBidirectional)
	).apply(instance, AngleSpeedConfig::new));

	private FloatRange impulse;
	private boolean impulseBidirectional;
	private float acceleration;
	private boolean accelerationBidirectional;

	public static AngleSpeedConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	public float getImpulseBidirectional(Random random) {
		return this.impulse.getRandom(random) * (this.impulseBidirectional && random.nextBoolean() ? -1 : 1);
	}

	public float getAccelerationBidirectional(Random random) {
		return this.acceleration * (this.accelerationBidirectional && random.nextBoolean() ? -1 : 1);
	}

}
