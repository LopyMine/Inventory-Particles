package net.lopymine.ip.element.base;

import net.lopymine.ip.element.InventoryCursor;

public interface IHoverableElement {

	void setHovered(boolean bl);

	boolean isHovered();

	default void updateHovered(InventoryCursor cursor, float x, float y, float width, float height) {
		boolean bl = cursor.getMouseX() > x && cursor.getMouseX() < x + width && cursor.getMouseY() > y && cursor.getMouseY() < y + height;
		this.setHovered(bl);
	}

}
