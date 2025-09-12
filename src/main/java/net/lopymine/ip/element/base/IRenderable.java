package net.lopymine.ip.element.base;

import net.lopymine.ip.element.InventoryCursor;
import net.minecraft.client.gui.DrawContext;

public interface IRenderable {

	void render(DrawContext context, InventoryCursor cursor, float tickProgress, boolean stoppedTicking);

}
