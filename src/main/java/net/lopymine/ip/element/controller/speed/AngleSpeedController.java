package net.lopymine.ip.element.controller.speed;

import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.RandomSource;

public class AngleSpeedController<T extends IRotatableElement & IMovableElement> extends AbstractSpeedController<AngleSpeedController<T>, T> {

	public AngleSpeedController(SpeedConfig config, RandomSource random) {
		super(config, random, 0.0D);
	}

	@Override
	protected AngleSpeedController<T> getController() {
		return this;
	}
}
