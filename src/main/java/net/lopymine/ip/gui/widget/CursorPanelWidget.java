package net.lopymine.ip.gui.widget;

import lombok.*;
import net.lopymine.ip.element.InventoryCursor;
import net.minecraft.client.gui.*;

@Getter
@Setter
public class CursorPanelWidget implements Drawable {

	private InventoryCursor cursor = new InventoryCursor();

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {

	}

}
