package net.lopymine.ip.controller.modifier.speed;

import net.lopymine.ip.controller.modifier.IControllerModifier;
import net.lopymine.ip.controller.speed.AbstractSpeedController;
import net.lopymine.ip.element.base.IMovableElement;

public interface ISpeedControllerModifier<C extends AbstractSpeedController<C, E>, E extends IMovableElement> extends IControllerModifier<C, E> {

	double getImpulse(E element);

	double getAcceleration(E element);

	double getBraking(E element);

	double getTurbulence(E element);

	@Override
	default void modify(C controller, E element) { }

	@Override
	default void tick(E element) { }
}
