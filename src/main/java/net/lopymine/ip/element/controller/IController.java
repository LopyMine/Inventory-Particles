package net.lopymine.ip.element.controller;

import net.lopymine.ip.debug.IDebugRenderable;

public interface IController<E> extends IDebugRenderable {

	void tick(E element);

}
