package net.lopymine.ip.modmenu;

import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.yacl.YACLConfigurationScreen;
import net.lopymine.mossylib.modmenu.AbstractModMenuIntegration;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration extends AbstractModMenuIntegration {

	@Override
	protected String getModId() {
		return InventoryParticles.MOD_ID;
	}

	@Override
	protected Screen createConfigScreen(Screen screen) {
		return YACLConfigurationScreen.createScreen(screen);
	}
}
