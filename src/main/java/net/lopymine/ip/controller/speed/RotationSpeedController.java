package net.lopymine.ip.controller.speed;

import lombok.*;
import net.lopymine.ip.config.particle.ParticlePhysics.RotationSpeedPhysics.RotationConfig;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public class RotationSpeedController<T extends IRotatableElement & IMovableElement> extends AbstractSpeedController<RotationSpeedController<T>, T> {

	private boolean rotateInMovementDirection;
	private float rotation;

	public RotationSpeedController(RotationConfig config, Random random) {
		super(config.getSpeedConfig(), random, 0.0F);
		this.rotateInMovementDirection = config.isRotateInMovementDirection();
	}

	@Override
	public void tick(T element) {
		super.tick(element);

		if (!this.rotateInMovementDirection) {
			return;
		}

		float deltaX = element.getX() - element.getLastX();
		float deltaY = element.getY() - element.getLastY();

		if (deltaX == 0.0F && deltaY == 0.0F) {
			return;
		}

		float movementRotationRad = (float) Math.atan2(-deltaY, -deltaX);
		float rotationDegrees = (float) Math.toDegrees(movementRotationRad) - 90F;
		this.rotation = rotationDegrees % 360F;
	}
}
