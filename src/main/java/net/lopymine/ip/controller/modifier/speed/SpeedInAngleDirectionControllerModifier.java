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
	public double getImpulse(E element) {
		return this.calc(this.config.getImpulseBidirectional(this.random), element);
	}

	@Override
	public double getAcceleration(E element) {
		return this.calc(this.config.getAccelerationBidirectional(this.random), element);
	}

	@Override
	public double getBraking(E element) {
		return Math.abs(this.calc(this.config.getBraking(), element));
	}

	@Override
	public double getTurbulence(E element) {
		return this.calc(this.config.getTurbulence().getRandom(this.random), element);
	}

	public double calc(double multiplier, E element) {
		if (multiplier == 0.0D) {
			return 0.0D;
		}
		double value = Math.toRadians(element.getAngle());
		return (this.xAxis ? Math.sin(value) : -Math.cos(value)) * multiplier;
	}
}
