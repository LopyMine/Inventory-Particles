package net.lopymine.ip.controller.speed;

import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.element.base.IMovableElement;
import net.minecraft.util.math.random.Random;

public class SpeedController extends AbstractSpeedController<IMovableElement> {

	public SpeedController(SpeedConfig config, Random random, float cursorSpeed) {
		super(config, random);
		this.speed += (cursorSpeed * config.getCursorImpulseInheritCoefficient());
	}

}
