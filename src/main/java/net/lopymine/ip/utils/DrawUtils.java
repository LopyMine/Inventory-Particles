package net.lopymine.ip.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;

public class DrawUtils {

	public static void drawParticleSprite(
			DrawContext context,
			Sprite sprite,
			int x,
			int y,
			float width,
			float height,
			int color
	) {
		//? if <=1.21.4 {
		/*context.drawSpriteStretched(net.minecraft.client.render.RenderLayer::getGuiTexturedOverlay, sprite, x, y, (int) width, (int) height, color);
		*///?} else {
		context.drawTexturedQuad(
				/*? if >=1.21.6 {*/ net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, /*?} elif >=1.21.2 {*/ /*net.minecraft.client.render.RenderLayer::getGuiTextured,*//*?}*/
				sprite.getAtlasId(),
				x,
				(int) (x + width),
				y,
				(int) (y + height),
				sprite.getMinU(),
				sprite.getMaxU(),
				sprite.getMinV(),
				sprite.getMaxV(),
				color
		);
		//?}
	}
}
