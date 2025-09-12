package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.config.range.DoubleRange;
import net.lopymine.ip.config.speed.*;
import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class ParticlePhysics {

	public static final Codec<ParticlePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("base", BasePhysics.getNewInstance(), BasePhysics.CODEC, ParticlePhysics::getBase),
			option("rotation", RotationSpeedPhysics.getNewInstance(), RotationSpeedPhysics.CODEC, ParticlePhysics::getRotation)
	).apply(instance, ParticlePhysics::new));

	private BasePhysics base;
	private RotationSpeedPhysics rotation;

	public static ParticlePhysics getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class BasePhysics {

		public static final Codec<BasePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("x_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, BasePhysics::getXSpeed),
				option("y_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, BasePhysics::getYSpeed),
				option("angle_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, BasePhysics::getAngleSpeed)
		).apply(instance, BasePhysics::new));

		private SpeedConfig xSpeed;
		private SpeedConfig ySpeed;
		private SpeedConfig angleSpeed;

		public static BasePhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class RotationSpeedPhysics {

		public static final Codec<RotationSpeedPhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("particle", RotationConfig.getNewInstance(), RotationConfig.CODEC, RotationSpeedPhysics::getParticleRotationConfig),
				option("texture", RotationConfig.getNewInstance(), RotationConfig.CODEC, RotationSpeedPhysics::getTextureRotationConfig)
		).apply(instance, RotationSpeedPhysics::new));

		private RotationConfig particleRotationConfig;
		private RotationConfig textureRotationConfig;

		public static RotationSpeedPhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

		@Getter
		@Setter
		@AllArgsConstructor
		public static class RotationConfig {

			public static final Codec<RotationConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					option("spawn_angle", new DoubleRange(), DoubleRange.CODEC, RotationConfig::getSpawnAngle),
					option("rotate_in_movement_direction", false, Codec.BOOL, RotationConfig::isRotateInMovementDirection),
					option("speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, RotationConfig::getSpeedConfig)
			).apply(instance, RotationConfig::new));

			private DoubleRange spawnAngle;
			private boolean rotateInMovementDirection;
			private SpeedConfig speedConfig;

			public static RotationConfig getNewInstance() {
				return CodecUtils.parseNewInstanceHacky(CODEC);
			}

		}

	}
}