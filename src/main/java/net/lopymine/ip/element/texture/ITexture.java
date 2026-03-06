package net.lopymine.ip.element.texture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public interface ITexture {

	void clear();

	void render(GuiGraphics graphics, float x, float y, float width, float height, int color);

	Identifier getId();

	void initialize();

}
