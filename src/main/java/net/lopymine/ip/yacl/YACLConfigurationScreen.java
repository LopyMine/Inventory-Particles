package net.lopymine.ip.yacl;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.yacl.category.*;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;
import net.minecraft.client.gui.screens.Screen;

@ExtensionMethod(SimpleOptionExtension.class)
public class YACLConfigurationScreen {

	private YACLConfigurationScreen() {
		throw new IllegalStateException("Screen class");
	}

	public static Screen createScreen(Screen parent) {
		InventoryParticlesConfig defConfig = InventoryParticlesConfig.getNewInstance();
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();

		Runnable onSave = () -> {
			config.getWhitelistsConfig().recompileAll();
			config.getFamilyGenerationConfig().recompileAll();
			config.saveAsync();
		};
		return SimpleYACLScreen.startBuilder(InventoryParticles.MOD_ID, parent, onSave)
				.categories(GeneralCategory.get(defConfig, config))
				.categories(ParticlesSpawnCategory.get(defConfig, config))
				.build();
	}

}
