package net.lopymine.ip.client.renderer;

import java.util.*;
import net.lopymine.ip.element.*;
import net.lopymine.ip.renderer.*;
import net.minecraft.client.gui.DrawContext;

public class DebugCursorInfoRenderer extends AbstractDebugInfoRenderer {

	@Override
	public void render(DrawContext context) {
		this.render(context, InventoryCursor.class, InventoryParticlesRenderer.getInstance().getCursor());
	}

	@Override
	public void render(DrawContext context, Class<?> clazz, Object clazzInstance) {
		super.render(context, clazz, clazzInstance);
		Collection<IParticle> screenParticles = InventoryParticlesRenderer.getInstance().getScreenParticles();
		this.renderDecoration(context, "Misc");
		this.renderFieldData(context, "Total Particles", screenParticles.size());
	}

	@Override
	protected String getRendererName() {
		return "InventoryCursor";
	}

}
