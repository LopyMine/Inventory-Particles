package net.lopymine.mossy.yacl;

import dev.isxander.yacl3.api.*;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import net.lopymine.mossy.client.MossyClient;
import net.lopymine.mossy.config.MossyConfig;
import net.lopymine.mossy.utils.ModMenuUtils;
import net.lopymine.mossy.yacl.base.*;
import net.lopymine.mossy.yacl.extension.SimpleOptionExtension;
import net.lopymine.mossy.yacl.screen.SimpleYACLScreen;
import net.lopymine.mossy.yacl.utils.*;

import java.util.function.Function;

@ExtensionMethod(SimpleOptionExtension.class)
public class YACLConfigurationScreen {

	private static final Function<Boolean, Text> ENABLED_OR_DISABLE_FORMATTER = ModMenuUtils.getEnabledOrDisabledFormatter();

	private YACLConfigurationScreen() {
		throw new IllegalStateException("Screen class");
	}

	public static Screen createScreen(Screen parent) {
		MossyConfig defConfig = MossyConfig.getNewInstance();
		MossyConfig config = MossyConfig.getInstance();

		return SimpleYACLScreen.startBuilder(parent, config::saveAsync)
				.categories(getGeneralCategory(defConfig, config))
				.categories(SimpleCollector.getIf(getSecretCategory(defConfig, config), config::isMossy))
				.build();
	}

	private static ConfigCategory getSecretCategory(MossyConfig defConfig, MossyConfig config) {
		return SimpleCategory.startBuilder("secret_category")
				.groups(getSecretGroup(defConfig, config))
				.build();
	}

	private static ConfigCategory getGeneralCategory(MossyConfig defConfig, MossyConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMossyGroup(defConfig, config))
				.build();
	}

	private static OptionGroup getSecretGroup(MossyConfig defConfig, MossyConfig config) {
		return SimpleGroup.startBuilder("secret_group").options(
				SimpleOption.<Float>startBuilder("secret_option")
						.withBinding(defConfig.getSecret(), config::getSecret, config::setSecret, false)
						.withController(-180F, 180F, 1.0F)
						.withDescription(SimpleContent.NONE)
						.build()
		).build();
	}

	private static OptionGroup getMossyGroup(MossyConfig defConfig, MossyConfig config) {
		return SimpleGroup.startBuilder("mossy_group").options(
				SimpleOption.<Boolean>startBuilder("mossy_option")
						.withBinding(defConfig.isMossy(), config::isMossy, config::setMossy, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.withDescription(SimpleContent.IMAGE)
						.build()
		).build();
	}

}


