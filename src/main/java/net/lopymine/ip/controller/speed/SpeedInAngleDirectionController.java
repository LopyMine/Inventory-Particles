package net.lopymine.ip.controller.speed;

import net.lopymine.ip.config.speed.angle.AngleSpeedConfig;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

public class SpeedInAngleDirectionController<T extends IMovableElement & IRotatableElement> implements IController<T> {

	@HideInDebugRender
	private final AngleSpeedConfig config;
	@HideInDebugRender
	private final Random random;

	public SpeedInAngleDirectionController(AngleSpeedConfig config, Random random,T element) {
		this.config = config;
		this.random = random;
		this.controlSpeed(element, config.getImpulseBidirectional(random));
	}

	@Override
	public void tick(T element) {
		this.controlSpeed(element, this.config.getAccelerationBidirectional(this.random));
	}

	private void controlSpeed(T element, float multiplier) {
		float angle = element.getAngle();
		element.setSpeedX(element.getSpeedX() - (float) (Math.sin(-Math.toRadians(angle)) * multiplier));
		element.setSpeedY(element.getSpeedY() - (float) (Math.cos(-Math.toRadians(angle)) * multiplier));
	}
}
