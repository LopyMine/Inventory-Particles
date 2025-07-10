package net.lopymine.mossy.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.*;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

@SuppressWarnings("unused")
public class DrawUtils {

	public static void drawTexture(DrawContext context, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		context.drawTexture(/*? >=1.21.2 {*//*RenderLayer::getGuiTextured,*//*?}*/ sprite, x, y, u, v, width, height, textureWidth, textureHeight);
	}

	public static void drawTooltip(DrawContext context, List<TooltipComponent> list, int x, int y) {
		context.drawTooltip(MinecraftClient.getInstance().textRenderer, list, x, y, HoveredTooltipPositioner.INSTANCE /*? >=1.21.2 {*//*,null*//*?}*/);
	}

	public static void drawCenteredText(DrawContext context, int x, int y, int width, Text text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int textWidth = textRenderer.getWidth(text);

		int centerX = x + (width / 2);
		int start = centerX - (textWidth / 2);
		int end = centerX + (textWidth / 2);

		if (start < x || end > x + width) {
			ClickableWidget.drawScrollableText(context, textRenderer, text, x, y, x + width, y + textRenderer.fontHeight, -1);
		} else {
			context.drawText(textRenderer, text, start, y, -1, true);
		}
	}

	public static void drawText(DrawContext context, int x, int y, int width, Text text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int textWidth = textRenderer.getWidth(text);
		if (x + textWidth > x + width) {
			ClickableWidget.drawScrollableText(context, textRenderer, text, x, y, x + width, y + textRenderer.fontHeight, -1);
		} else {
			context.drawText(textRenderer, text, x, y, -1, true);
		}
	}
}
