package net.lopymine.ip.client.renderer;

import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.element.*;
import net.lopymine.ip.renderer.InvParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class CursorDebugInfoRenderer {

	private int yOffset = 0;

	public void render(DrawContext context) {
		this.yOffset = 5;

		Cursor cursor = InvParticlesRenderer.getInstance().getCursor();
		List<InvParticle> screenParticles = InvParticlesRenderer.getInstance().getScreenParticles();
		renderLabel(context, "Cursor Last Speed X", cursor.getLastSpeedX());
		renderLabel(context, "Cursor Last Speed Y", cursor.getLastSpeedY());
		renderLabel(context, "Cursor Last Speed", cursor.getLastSpeed());
		renderLabel(context, "Cursor Last Tick X", cursor.getLastX());
		renderLabel(context, "Cursor Last Tick Y", cursor.getLastY());
		renderLabel(context, "Cursor Speed X", cursor.getSpeedX());
		renderLabel(context, "Cursor Speed Y", cursor.getSpeedY());
		renderLabel(context, "Cursor Speed", cursor.getSpeed());
		renderLabel(context, "Cursor Tick X", cursor.getX());
		renderLabel(context, "Cursor Tick Y", cursor.getY());
		renderLabel(context, "Cursor Mouse X", cursor.getMouseX());
		renderLabel(context, "Cursor Mouse Y", cursor.getMouseY());
		renderLabel(context, "Cursor Speed X Delta", cursor.getDeltaSpeedX());
		renderLabel(context, "Cursor Speed Y Delta", cursor.getDeltaSpeedY());
		renderLabel(context, "Cursor Speed Delta", cursor.getDeltaSpeed());
		renderLabel(context, "Cursor Tick X Delta", cursor.getDeltaX());
		renderLabel(context, "Cursor Tick Y Delta", cursor.getDeltaY());
		renderLabel(context, "Cursor Item", cursor.getCurrentItem());
		renderLabel(context, "Total Particles", screenParticles.size());

		this.yOffset = 0;
	}

	private void renderLabel(DrawContext context, String name, Object text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		context.drawText(textRenderer, name + ": " + (text == null ? "null" : text.toString()), 5, this.yOffset, -1, true);
		this.yOffset += textRenderer.fontHeight + 2;
	}

}
