package net.lopymine.ip.yacl;

import dev.isxander.yacl3.api.*;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.utils.ModMenuUtils;
import net.lopymine.ip.yacl.base.*;
import net.lopymine.ip.yacl.extension.SimpleOptionExtension;
import net.lopymine.ip.yacl.screen.SimpleYACLScreen;
import net.lopymine.ip.yacl.utils.*;

import java.util.function.Function;

@ExtensionMethod(SimpleOptionExtension.class)
public class YACLConfigurationScreen {

	private static final Function<Boolean, Text> ENABLED_OR_DISABLE_FORMATTER = ModMenuUtils.getEnabledOrDisabledFormatter();

	private YACLConfigurationScreen() {
		throw new IllegalStateException("Screen class");
	}

	public static Screen createScreen(Screen parent) {
		InventoryParticlesConfig defConfig = InventoryParticlesConfig.getNewInstance();
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();

		return SimpleYACLScreen.startBuilder(parent, config::saveAsync)
				.categories(getGeneralCategory(defConfig, config))
				.categories(SimpleCollector.getIf(getSecretCategory(defConfig, config), config::isMossy))
				.build();
	}

	private static ConfigCategory getSecretCategory(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleCategory.startBuilder("secret_category")
				.groups(getSecretGroup(defConfig, config))
				.build();
	}

	private static ConfigCategory getGeneralCategory(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMossyGroup(defConfig, config))
				.build();
	}

	private static OptionGroup getSecretGroup(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleGroup.startBuilder("secret_group").options(
				SimpleOption.<Float>startBuilder("secret_option")
						.withBinding(defConfig.getSecret(), config::getSecret, config::setSecret, false)
						.withController(-180F, 180F, 1.0F)
						.withDescription(SimpleContent.NONE)
						.build()
		).build();
	}

	private static OptionGroup getMossyGroup(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleGroup.startBuilder("mossy_group").options(
				SimpleOption.<Boolean>startBuilder("mossy_option")
						.withBinding(defConfig.isMossy(), config::isMossy, config::setMossy, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.withDescription(SimpleContent.IMAGE)
						.build()
		).build();
	}

}


