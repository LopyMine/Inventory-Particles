package net.lopymine.ip.family.gui;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ParticlesLinkingInfo {

	public static final Identifier LOADING_0 = InventoryParticles.id("textures/gui/loading_0.png");
	public static final Identifier LOADING_1 = InventoryParticles.id("textures/gui/loading_1.png");

	public static void render(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		int progress = ParticlesConfigsManager.LINKED_ITEMS.get();
		int total = ParticlesConfigsManager.TOTAL_ITEMS.get();
		if (progress == -1 || progress == total) {
			return;
		}

		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 8 && mouseY < y + 8;
		if (hovered) {
			List<ClientTooltipComponent> components = new ArrayList<>();
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.title").getVisualOrderText()));
			components.add(ClientTooltipComponent.create(InventoryParticles.text("particles_linking.loading", progress, total).getVisualOrderText()));

			DrawUtils.drawTooltip(graphics, components, mouseX, mouseY + 15);
		}
		DrawUtils.drawTexture(graphics, getSprite(), x, y, 0, 0, 8, 8, 8, 8);
	}

	private static Identifier getSprite() {
		return Util.getMillis() / 1000L % 2L == 0L ? LOADING_0 : LOADING_1;
	}

}
