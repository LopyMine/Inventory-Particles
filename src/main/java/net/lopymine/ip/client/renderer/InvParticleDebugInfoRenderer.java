package net.lopymine.ip.client.renderer;

import net.lopymine.ip.element.*;
import net.lopymine.ip.renderer.InvParticlesRenderer;
import net.lopymine.ip.texture.ITextureProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;

public class InvParticleDebugInfoRenderer {

	private int yOffset = 0;

	public void render(DrawContext context) {
		this.yOffset = 5;

		InvParticle hoveredParticle = InvParticlesRenderer.getInstance().getHoveredParticle();
		if (hoveredParticle == null) {
			return;
		}

		renderLabel(context, "Particle Acceleration X", hoveredParticle.getAccelerationX());
		renderLabel(context, "Particle Acceleration Y", hoveredParticle.getAccelerationY());
		renderLabel(context, "Particle Acceleration Rotation", hoveredParticle.getAccelerationRotation());
		renderLabel(context, "Particle Last X", hoveredParticle.getLastX());
		renderLabel(context, "Particle Last Y", hoveredParticle.getLastY());
		renderLabel(context, "Particle Last Rotation", hoveredParticle.getLastRotation());
		renderLabel(context, "Particle X", hoveredParticle.getX());
		renderLabel(context, "Particle Y", hoveredParticle.getY());
		renderLabel(context, "Particle Rotation", hoveredParticle.getRotation());
		renderLabel(context, "Particle Speed X", hoveredParticle.getSpeedX());
		renderLabel(context, "Particle Speed Y", hoveredParticle.getSpeedY());
		renderLabel(context, "Particle Speed Rotation", hoveredParticle.getSpeedRotation());
		renderLabel(context, "Particle Dead", hoveredParticle.isDead());
		renderLabel(context, "Particle Texture", hoveredParticle.getTexture());
		renderLabel(context, "Particle Ticks", hoveredParticle.getTicks());
		ITextureProvider textureProvider = hoveredParticle.getTextureProvider();
		textureProvider.debugRender((s, o) -> this.renderLabel(context, s, o));

		this.yOffset = 0;
	}

	private void renderLabel(DrawContext context, String name, Object text) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if (screen == null) {
			return;
		}
		String string = name + ": " + (text == null ? "null" : text.toString());
		int x = screen.width - textRenderer.getWidth(string) - 5;
		context.drawText(textRenderer, string, x, this.yOffset, -1, true);
		this.yOffset += textRenderer.fontHeight + 2;
	}
}
