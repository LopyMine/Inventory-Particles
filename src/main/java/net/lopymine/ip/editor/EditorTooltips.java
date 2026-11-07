package net.lopymine.ip.editor;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.network.chat.Component;

public class EditorTooltips {

	private static boolean ENABLED = true;

	@SuppressWarnings("all")
	public static boolean isEnabled() {
		return ENABLED;
	}

	public static void toggle() {
		ENABLED = !ENABLED;
	}

	public static Component getToggleText() {
		return InventoryParticles.text(ENABLED ? "editor.config.tooltips.on" : "editor.config.tooltips.off");
	}

	public static List<Component> of(Component... lines) {
		return List.of(lines);
	}

	public static List<Component> ofKeys(String... keys) {
		List<Component> lines = new ArrayList<>();
		for (String key : keys) {
			lines.add(InventoryParticles.text(key));
		}
		return lines;
	}
}
