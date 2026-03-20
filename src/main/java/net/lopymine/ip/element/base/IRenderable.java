package net.lopymine.ip.element.base;

import net.lopymine.ip.element.mod.InventoryCursor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IRenderable {

	void render(GuiGraphicsExtractor context, InventoryCursor cursor, float tickProgress, boolean stoppedTicking);

}
