package net.lopymine.ip.controller.modifier.speed;

import net.lopymine.ip.controller.modifier.IControllerModifier;
import net.lopymine.ip.controller.speed.AbstractSpeedController;
import net.lopymine.ip.element.base.IMovableElement;

public interface ISpeedControllerModifier<C extends AbstractSpeedController<C, E>, E extends IMovableElement> extends IControllerModifier<C, E> {

	float getImpulse(E element);

	float getAcceleration(E element);

	float getBraking(E element);

	float getTurbulence(E element);

	@Override
	default void modify(C controller, E element) { }

	@Override
	default void tick(E element) { }
}
