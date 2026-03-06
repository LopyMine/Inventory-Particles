package net.lopymine.ip.element.controller.speed;

import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.element.base.IMovableElement;
import net.minecraft.util.RandomSource;

public class SpeedController<E extends IMovableElement> extends AbstractSpeedController<SpeedController<E>, E> {

	public SpeedController(SpeedConfig config, RandomSource random, double impulse) {
		super(config, random, impulse * config.getCursorImpulseInheritCoefficient());
	}

	@Override
	protected SpeedController<E> getController() {
		return this;
	}
}
