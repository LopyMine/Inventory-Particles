package net.lopymine.ip.element.base;

import net.lopymine.ip.element.mod.InventoryCursor;
import net.minecraft.client.gui.GuiGraphics;

public interface IRenderable {

	void render(GuiGraphics context, InventoryCursor cursor, float tickProgress, boolean stoppedTicking);

}
