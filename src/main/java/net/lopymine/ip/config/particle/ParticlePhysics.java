package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.config.speed.angle.AngleSpeedConfig;
import net.lopymine.ip.config.range.FloatRange;
import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class ParticlePhysics {

	public static final Codec<ParticlePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("base", BasePhysics.getNewInstance(), BasePhysics.CODEC, ParticlePhysics::getBase),
			option("rotation", RotationSpeedPhysics.getNewInstance(), RotationSpeedPhysics.CODEC, ParticlePhysics::getRotation),
			option("angle", AnglePhysics.getNewInstance(), AnglePhysics.CODEC, ParticlePhysics::getAngle)
	).apply(instance, ParticlePhysics::new));

	private BasePhysics base;
	private RotationSpeedPhysics rotation;
	private AnglePhysics angle;

	public static ParticlePhysics getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	@Getter
	@AllArgsConstructor
	public static class BasePhysics {

		public static final Codec<BasePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("x_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, BasePhysics::getXSpeed),
				option("y_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, BasePhysics::getYSpeed)
		).apply(instance, BasePhysics::new));

		private SpeedConfig xSpeed;
		private SpeedConfig ySpeed;

		public static BasePhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

	}

	@Getter
	@AllArgsConstructor
	public static class RotationSpeedPhysics {

		public static final Codec<RotationSpeedPhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("rotate_in_movement_direction", false, Codec.BOOL, RotationSpeedPhysics::isRotateInMovementDirection),
				option("rotation_speed", SpeedConfig.getNewInstance(), SpeedConfig.CODEC, RotationSpeedPhysics::getRotationSpeed)
		).apply(instance, RotationSpeedPhysics::new));

		private boolean rotateInMovementDirection;
		private SpeedConfig rotationSpeed;

		public static RotationSpeedPhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}
	}

	@Getter
	@AllArgsConstructor
	public static class AnglePhysics {

		public static final Codec<AnglePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("spawn_angle", new FloatRange(), FloatRange.CODEC, AnglePhysics::getSpawnAngle),
				option("speed_in_angle_direction_enabled", false, Codec.BOOL, AnglePhysics::isSpeedInAngleDirectionEnabled),
				option("speed_in_angle_direction", AngleSpeedConfig.getNewInstance(), AngleSpeedConfig.CODEC, AnglePhysics::getAngleSpeedConfig)
		).apply(instance, AnglePhysics::new));

		private FloatRange spawnAngle;
		private boolean isSpeedInAngleDirectionEnabled;
		private AngleSpeedConfig angleSpeedConfig;

		public static AnglePhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

	}
}