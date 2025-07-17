package net.lopymine.ip.controller.speed;

import lombok.Getter;
import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

@Getter
public class RotationSpeedController<T extends IRotatableElement & IMovableElement> extends AbstractSpeedController<T> {

	private final boolean rotateInMovementDirection;
	private float rotation;

	public RotationSpeedController(SpeedConfig config, Random random, boolean rotateInMovementDirection) {
		super(config, random);
		this.rotateInMovementDirection = rotateInMovementDirection;
	}

	@Override
	public void tick(T element) {
		super.tick(element);

		if (!this.rotateInMovementDirection) {
			return;
		}

		float deltaX = element.getX() - element.getLastX();
		float deltaY = element.getY() - element.getLastY();

		float movementRotationRad = (float) Math.atan2(-deltaY, -deltaX);
		float rotationDegrees = (float) Math.toDegrees(movementRotationRad) - 90F;
		this.rotation = rotationDegrees % 360F;
	}
}
