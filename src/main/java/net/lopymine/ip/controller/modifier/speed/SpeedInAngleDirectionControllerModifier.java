package net.lopymine.ip.controller.modifier.speed;

import lombok.*;
import net.lopymine.ip.config.speed.SpeedConfig;
import net.lopymine.ip.controller.speed.*;
import net.lopymine.ip.element.base.*;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public class SpeedInAngleDirectionControllerModifier<E extends IMovableElement & IRotatableElement> implements ISpeedControllerModifier<SpeedController<E>, E> {

	private SpeedConfig config;
	private Random random;
	private boolean xAxis;

	public SpeedInAngleDirectionControllerModifier(SpeedConfig config, Random random, boolean xAxis) {
		this.config = config;
		this.random = random;
		this.xAxis  = xAxis;
	}

	@Override
	public float getImpulse(E element) {
		return this.calc(this.config.getImpulseBidirectional(this.random), element);
	}

	@Override
	public float getAcceleration(E element) {
		return this.calc(this.config.getAccelerationBidirectional(this.random), element);
	}

	@Override
	public float getBraking(E element) {
		return Math.abs(this.calc(this.config.getBraking(), element));
	}

	@Override
	public float getTurbulence(E element) {
		return this.calc(this.config.getTurbulence().getRandom(this.random), element);
	}

	public float calc(float multiplier, E element) {
		if (multiplier == 0.0F) {
			return 0F;
		}
		double value = Math.toRadians(element.getAngle());
		return (float) ((this.xAxis ? Math.sin(value) : -Math.cos(value)) * multiplier);
	}
}
