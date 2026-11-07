package net.lopymine.ip.element.controller.modifier.speed;

import lombok.*;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.controller.speed.*;

@Getter
@Setter
public class SpeedInAngleDirectionControllerModifier<E extends IMovableElement & IRotatableElement> implements ISpeedControllerModifier<SpeedController<E>, E> {

	private AngleSpeedController<E> angleSpeedController;
	private boolean xAxis;

	private double appliedSpeed;

	public SpeedInAngleDirectionControllerModifier(AngleSpeedController<E> angleSpeedController, boolean xAxis) {
		this.angleSpeedController = angleSpeedController;
		this.xAxis                = xAxis;
	}

	@Override
	public double getImpulse(E element) {
		return 0.0D;
	}

	@Override
	public double getAcceleration(E element) {
		return 0.0D;
	}

	@Override
	public double getBraking(E element) {
		return 0.0D;
	}

	@Override
	public double getTurbulence(E element) {
		return 0.0D;
	}

	@Override
	public void modify(SpeedController<E> controller, E element) {
		double speed = this.calc(this.angleSpeedController.getSpeed(), element);
		controller.setSpeed(controller.getSpeed() - this.appliedSpeed + speed);
		this.appliedSpeed = speed;
	}

	public double calc(double multiplier, E element) {
		if (multiplier == 0.0D) {
			return 0.0D;
		}
		double value = Math.toRadians(element.getAngle());
		return (this.xAxis ? Math.sin(value) : -Math.cos(value)) * multiplier;
	}
}
