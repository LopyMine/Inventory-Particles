package net.lopymine.ip.controller.speed;

import lombok.*;
import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.IMovableElement;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public abstract class AbstractSpeedController<T extends IMovableElement> implements IController<T> {

	@HideInDebugRender
	protected final SpeedConfig config;
	@HideInDebugRender
	protected final Random random;
	protected float lastSpeed;
	protected float speed;

	public AbstractSpeedController(SpeedConfig config, Random random) {
		this.config = config;
		this.speed  = config.getImpulseBidirectional(random);
		this.random = random;
	}

	@Override
	public void tick(T element) {
		this.lastSpeed = this.speed;

		if (this.speed < this.config.getMax()) {
			this.speed += this.config.getAccelerationBidirectional(this.random);
			if (this.speed >= this.config.getMax()) {
				this.speed = this.config.getMax();
			}
		}

		if (this.speed > 0) {
			this.speed -= this.config.getBraking();
			if (this.speed <= 0) {
				this.speed = 0;
			}
		}

		if (this.speed < 0) {
			this.speed += this.config.getBraking();
			if (this.speed >= 0) {
				this.speed = 0;
			}
		}

		this.speed += this.config.getTurbulence().getRandom(this.random);
	}
}
