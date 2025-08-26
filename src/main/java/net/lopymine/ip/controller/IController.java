package net.lopymine.ip.controller;

import net.lopymine.ip.debug.IDebugRenderable;

public interface IController<E> extends IDebugRenderable {

	void tick(E element);

}
