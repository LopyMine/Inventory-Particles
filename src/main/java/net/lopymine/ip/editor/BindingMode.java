package net.lopymine.ip.editor;

import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.network.chat.Component;

@Getter
public enum BindingMode {

	LINK("link"),
	COPY("copy"),
	OVERRIDE("override");

	private final String name;

	BindingMode(String name) {
		this.name = name;
	}

	public Component getTitle() {
		return InventoryParticles.text("editor.binding_mode.%s".formatted(this.name));
	}

	public Component getDescription() {
		return InventoryParticles.text("editor.binding_mode.%s.description".formatted(this.name));
	}

	public BindingMode next() {
		BindingMode[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}
}
