package net.lopymine.ip.element.texture;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public interface ITexture {

	void clear();

	void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color);

	Identifier getId();

	void initialize();

}
