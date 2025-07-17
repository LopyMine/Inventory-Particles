package net.lopymine.ip.yacl;

import dev.isxander.yacl3.api.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.utils.ModMenuUtils;
import net.lopymine.ip.yacl.base.*;
import net.lopymine.ip.yacl.extension.SimpleOptionExtension;
import net.lopymine.ip.yacl.screen.SimpleYACLScreen;

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
				.build();
	}

	private static ConfigCategory getGeneralCategory(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMainGroup(defConfig, config))
				.groups(getParticlesGroup(defConfig, config))
				.build();
	}

	private static OptionGroup getMainGroup(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleGroup.startBuilder("main_group").options(
				SimpleOption.<Boolean>startBuilder("mod_enabled")
						.withBinding(defConfig.isModEnabled(), config::isModEnabled, config::setModEnabled, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.build(),
				SimpleOption.<Boolean>startBuilder("debug_mode_enabled")
						.withBinding(defConfig.isDebugModeEnabled(), config::isDebugModeEnabled, config::setDebugModeEnabled, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.build()
		).build();
	}

	private static OptionGroup getParticlesGroup(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleGroup.startBuilder("particles_group").options(
				SimpleOption.<Integer>startBuilder("maximum_particles_limit")
						.withBinding(defConfig.getMaxParticles(), config::getMaxParticles, config::setMaxParticles, false)
						.withController(0, Integer.MAX_VALUE, 1, false)
						.build(),
				SimpleOption.<ParticleDeletionMode>startBuilder("particle_deletion_mode")
						.withBinding(defConfig.getParticleDeletionMode(), config::getParticleDeletionMode, config::setParticleDeletionMode, false)
						.withController(ParticleDeletionMode.class)
						.build()
		).build();
	}

}


