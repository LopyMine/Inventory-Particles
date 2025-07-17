package net.lopymine.ip.controller;

import net.lopymine.ip.debug.IDebugRenderable;

public interface IController<T> extends IDebugRenderable {

	void tick(T element);

}
