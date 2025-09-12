package net.lopymine.ip.controller.speed;

import lombok.*;
import net.lopymine.ip.config.particle.ParticlePhysics.RotationSpeedPhysics.RotationConfig;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public class RotationSpeedController<T extends IRotatableElement & IMovableElement> extends AbstractSpeedController<RotationSpeedController<T>, T> {

	private boolean rotateInMovementDirection;
	private double rotation;

	public RotationSpeedController(RotationConfig config, Random random) {
		super(config.getSpeedConfig(), random, 0.0D);
		this.rotateInMovementDirection = config.isRotateInMovementDirection();
	}

	@Override
	public void tick(T element) {
		super.tick(element);

		if (!this.rotateInMovementDirection) {
			return;
		}

		double deltaX = element.getX() - element.getLastX();
		double deltaY = element.getY() - element.getLastY();

		if (deltaX == 0.0F && deltaY == 0.0F) {
			return;
		}

		double movementRotationRad = Math.atan2(-deltaY, -deltaX);
		double rotationDegrees = Math.toDegrees(movementRotationRad) - 90F;
		this.rotation = rotationDegrees % 360F;
	}
}
