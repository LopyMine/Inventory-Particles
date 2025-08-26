package net.lopymine.ip.controller.modifier;

import net.lopymine.ip.controller.IController;

@SuppressWarnings("unused")
public interface IControllerModifier<C extends IController<E>, E> {

	void modify(C controller, E element);

	void tick(E element);

}
