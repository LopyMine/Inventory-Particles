package net.lopymine.ip.client.renderer;

import net.lopymine.ip.element.InventoryParticle;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class DebugParticleInfoRenderer extends AbstractDebugInfoRenderer {

	public DebugParticleInfoRenderer() {
		this.registerSpecialFieldDebugRenderer("color", (consumer, decoration, value) -> {
			if (!(value instanceof Integer color)) {
				return;
			}
			consumer.accept("Raw Color", color);
			consumer.accept("Color Alpha", ArgbUtils.getAlpha(color));
			consumer.accept("Color Red", ArgbUtils.getRed(color));
			consumer.accept("Color Green", ArgbUtils.getGreen(color));
			consumer.accept("Color Blue", ArgbUtils.getBlue(color));
		});
	}

	@Override
	public void render(DrawContext context) {
		InventoryParticlesRenderer renderer = InventoryParticlesRenderer.getInstance();
		InventoryParticle selectedParticle = renderer.getSelectedParticle();
		if (selectedParticle == null) {
			return;
		}
		this.render(context, InventoryParticle.class, selectedParticle);
	}

	@Override
	protected int getTextX(Screen screen, TextRenderer textRenderer, String string) {
		return screen.width - textRenderer.getWidth(string) - this.xOffset;
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
