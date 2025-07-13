package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.lopymine.ip.utils.CodecUtils;
import net.minecraft.util.math.random.Random;
import static net.lopymine.ip.utils.CodecUtils.option;

@Setter
@Getter
@AllArgsConstructor
public class InvParticlePhysics {

	public static final Codec<InvParticlePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("base", BasePhysics.getNewInstance(), BasePhysics.CODEC, InvParticlePhysics::getBase),
			option("air", AirPhysics.getNewInstance(), AirPhysics.CODEC, InvParticlePhysics::getAir),
			option("rotation", RotationPhysics.getNewInstance(), RotationPhysics.CODEC, InvParticlePhysics::getRotation)
	).apply(instance, InvParticlePhysics::new));

	private BasePhysics base;
	private AirPhysics air;
	private RotationPhysics rotation;

	public static InvParticlePhysics getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	@Setter
	@Getter
	@AllArgsConstructor
	public static class BasePhysics {

		public static final Codec<BasePhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("cursor_impulse_x", true, Codec.BOOL, BasePhysics::isCursorImpulseX),
				option("cursor_impulse_y", true, Codec.BOOL, BasePhysics::isCursorImpulseY),
				option("impulse_y", new TwoFloatRange(), TwoFloatRange.CODEC, BasePhysics::getImpulseY),
				option("impulse_y_bidirectional", false, Codec.BOOL, BasePhysics::isImpulseYBidirectional),
				option("impulse_x", new TwoFloatRange(), TwoFloatRange.CODEC, BasePhysics::getImpulseX),
				option("impulse_x_bidirectional", false, Codec.BOOL, BasePhysics::isImpulseXBidirectional),
				option("acceleration_y", 0.0F, Codec.FLOAT, BasePhysics::getAccelerationY),
				option("acceleration_y_bidirectional", false, Codec.BOOL, BasePhysics::isAccelerationYBidirectional),
				option("acceleration_x", 0.0F, Codec.FLOAT, BasePhysics::getAccelerationX),
				option("acceleration_x_bidirectional", false, Codec.BOOL, BasePhysics::isAccelerationXBidirectional)
		).apply(instance, BasePhysics::new));

		private boolean cursorImpulseX;
		private boolean cursorImpulseY;
		private TwoFloatRange impulseY;
		private boolean impulseYBidirectional;
		private TwoFloatRange impulseX;
		private boolean impulseXBidirectional;
		private float accelerationY;
		private boolean accelerationYBidirectional;
		private float accelerationX;
		private boolean accelerationXBidirectional;

		public static BasePhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

		public float getImpulseXMultiplier(Random random) {
			if (!this.impulseXBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}

		public float getImpulseYMultiplier(Random random) {
			if (!this.impulseYBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}

		public float getAccelerationXMultiplier(Random random) {
			if (!this.accelerationXBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}

		public float getAccelerationYMultiplier(Random random) {
			if (!this.accelerationYBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}
	}

	@Setter
	@Getter
	@AllArgsConstructor
	public static class AirPhysics {
		public static final Codec<AirPhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("braking_y", 0.0F, Codec.FLOAT, AirPhysics::getBrakingY),
				option("braking_x", 0.0F, Codec.FLOAT, AirPhysics::getBrakingX),
				option("terminal_velocity_y", Float.MAX_VALUE, Codec.FLOAT, AirPhysics::getTerminalVelocityY),
				option("terminal_velocity_x", Float.MAX_VALUE, Codec.FLOAT, AirPhysics::getTerminalVelocityX),
				option("turbulence_y", new TwoFloatRange(), TwoFloatRange.CODEC, AirPhysics::getTurbulenceY),
				option("turbulence_x", new TwoFloatRange(), TwoFloatRange.CODEC, AirPhysics::getTurbulenceX)
		).apply(instance, AirPhysics::new));

		private float brakingY;
		private float brakingX;
		private float terminalVelocityY;
		private float terminalVelocityX;
		private TwoFloatRange turbulenceY;
		private TwoFloatRange turbulenceX;

		public static AirPhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}
	}

	@Setter
	@Getter
	@AllArgsConstructor
	public static class RotationPhysics {

		public static final Codec<RotationPhysics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				option("spawn_angle", new TwoFloatRange(), TwoFloatRange.CODEC, RotationPhysics::getSpawnAngle),
				option("rotation_impulse", new TwoFloatRange(), TwoFloatRange.CODEC, RotationPhysics::getRotationImpulse),
				option("rotation_impulse_bidirectional", false, Codec.BOOL, RotationPhysics::isRotationImpulseBidirectional),
				option("rotation_acceleration", 0.0F, Codec.FLOAT, RotationPhysics::getRotationAcceleration),
				option("rotation_acceleration_bidirectional", false, Codec.BOOL, RotationPhysics::isRotationAccelerationBidirectional),
				option("rotation_braking", 0.0F, Codec.FLOAT, RotationPhysics::getRotationBraking),
				option("terminal_rotation_velocity", Float.MAX_VALUE, Codec.FLOAT, RotationPhysics::getTerminalRotationVelocity),
				option("rotation_by_movement", false, Codec.BOOL, RotationPhysics::isRotationByMovement)
				).apply(instance, RotationPhysics::new));

		private TwoFloatRange spawnAngle;
		private TwoFloatRange rotationImpulse;
		private boolean rotationImpulseBidirectional;
		private float rotationAcceleration;
		private boolean rotationAccelerationBidirectional;
		private float rotationBraking;
		private float terminalRotationVelocity;
		private boolean rotationByMovement;

		public static RotationPhysics getNewInstance() {
			return CodecUtils.parseNewInstanceHacky(CODEC);
		}

		public float getRotationImpulseMultiplayer(Random random) {
			if (!this.rotationImpulseBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}

		public float getRotationAccelerationMultiplayer(Random random) {
			if (!this.rotationAccelerationBidirectional) {
				return 1;
			}
			return random.nextBoolean() ? -1 : 1;
		}
	}
}