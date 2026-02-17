package net.lopymine.ip.client.renderer;

import java.util.*;
import net.lopymine.ip.element.*;
import net.lopymine.ip.renderer.*;
import net.minecraft.client.gui.GuiGraphics;

public class DebugCursorInfoRenderer extends AbstractDebugInfoRenderer {

	@Override
	public void render(GuiGraphics context) {
		this.render(context, InventoryCursor.class, InventoryParticlesRenderer.getInstance().getCursor());
	}

	@Override
	public void render(GuiGraphics context, Class<?> clazz, Object clazzInstance) {
		super.render(context, clazz, clazzInstance);
		Collection<InventoryParticle> screenParticles = InventoryParticlesRenderer.getInstance().getScreenElements();
		this.renderDecoration(context, "Misc");
		this.renderFieldData(context, "Total Particles", screenParticles.size());
	}

	@Override
	protected String getRendererName() {
		return "InventoryCursor";
	}

}
