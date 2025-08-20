package net.lopymine.ip.controller.speed;

import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

public class SpeedInAngleDirectionController<T extends IMovableElement & IRotatableElement> extends AbstractSpeedController<T> {

	public SpeedInAngleDirectionController(SpeedConfig config, Random random) {
		super(config, random);
	}

	@Override
	public void tick(T element) {
		super.tick(element);
		this.controlSpeed(element, this.speed);
	}

	private void controlSpeed(T element, float multiplier) {
		if (multiplier == 0.0F) {
			return;
		}
		float angle = element.getAngle();
		element.setSpeedX(element.getSpeedX() - (float) (Math.sin(-Math.toRadians(angle)) * multiplier));
		element.setSpeedY(element.getSpeedY() - (float) (Math.cos(-Math.toRadians(angle)) * multiplier));
	}
}
