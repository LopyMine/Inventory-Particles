package net.lopymine.ip.element.base;

import net.lopymine.ip.element.InventoryCursor;

public interface IHoverableElement {

	void setHovered(boolean bl);

	boolean isHovered();

	default void updateHovered(InventoryCursor cursor, int x, int y, int width, int height) {
		boolean bl = cursor.getMouseX() > x && cursor.getMouseX() < x + width && cursor.getMouseY() > y && cursor.getMouseY() < y + height;
		this.setHovered(bl);
	}

}
