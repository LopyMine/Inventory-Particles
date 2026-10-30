package net.lopymine.ip.yacl.screen;

import net.lopymine.mossylib.yacl.custom.MossyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ResourcePackGenerationScreen extends Screen implements MossyScreen {

	private final Screen parent;

	public ResourcePackGenerationScreen(Screen parent) {
		super(Component.literal(""));
		this.parent = parent;
	}



	@Override
	public void onClose() {
		Minecraft.getInstance().gui.setScreen(this.parent);
	}

}
