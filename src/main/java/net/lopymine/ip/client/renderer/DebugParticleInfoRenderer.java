package net.lopymine.ip.client.renderer;

import net.lopymine.ip.element.mod.InventoryParticle;
import net.lopymine.ip.renderer.*;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class DebugParticleInfoRenderer extends AbstractDebugInfoRenderer {

	public DebugParticleInfoRenderer() {
		this.registerSpecialFieldDebugRenderer("color", (consumer, decoration, value) -> {
			if (!(value instanceof Integer color)) {
				return;
			}
			consumer.accept("Raw Color", color);
			consumer.accept("Color Alpha", ArgbUtils2.getAlpha(color));
			consumer.accept("Color Red", ArgbUtils2.getRed(color));
			consumer.accept("Color Green", ArgbUtils2.getGreen(color));
			consumer.accept("Color Blue", ArgbUtils2.getBlue(color));
		});
	}

	@Override
	public void render(GuiGraphicsExtractor context) {
		InventoryParticlesRenderer renderer = InventoryParticlesRenderer.getInstance();
		InventoryParticle selectedParticle = renderer.getSelectedElement();
		this.render(context, InventoryParticle.class, selectedParticle);
	}

	@Override
	protected int getTextX(Screen screen, Font textRenderer, String string) {
		return screen.width - textRenderer.width(string) - this.xOffset;
	}

	@Override
	protected int getTextXOffset() {
		return 10;
	}

	@Override
	protected String getRendererName() {
		return "InventoryParticle";
	}
}
