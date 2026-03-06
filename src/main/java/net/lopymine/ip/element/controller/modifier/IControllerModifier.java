package net.lopymine.ip.element.controller.modifier;

import net.lopymine.ip.element.controller.IController;

@SuppressWarnings("unused")
public interface IControllerModifier<C extends IController<E>, E> {

	void modify(C controller, E element);

	void tick(E element);

}
