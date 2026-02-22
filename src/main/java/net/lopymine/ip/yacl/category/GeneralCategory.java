package net.lopymine.ip.yacl.category;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticlesDeletionMode;
import net.lopymine.ip.config.sub.*;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;

@ExtensionMethod(SimpleOptionExtension.class)
public class GeneralCategory {

	public static SimpleCategory get(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMainGroup(defConfig.getMainConfig(), config.getMainConfig()))
				.groups(getVisualGroup(defConfig.getParticleConfig(), config.getParticleConfig()));
	}

	private static SimpleGroup getMainGroup(InventoryParticlesMainConfig defConfig, InventoryParticlesMainConfig config) {
		return SimpleGroup.startBuilder("main").options(
				SimpleOption.<Boolean>startBuilder("mod_enabled")
						.withBinding(defConfig.isModEnabled(), config::isModEnabled, config::setModEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Boolean>startBuilder("debug_mode_enabled")
						.withBinding(defConfig.isDebugModeEnabled(), config::isDebugModeEnabled, config::setDebugModeEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Boolean>startBuilder("nbt_debug_mode_enabled")
						.withBinding(defConfig.isNbtDebugModeEnabled(), config::isNbtDebugModeEnabled, config::setNbtDebugModeEnabled, false)
						.withController()
						.withDescription(SimpleContent.NONE)
		);
	}

	private static SimpleGroup getVisualGroup(InventoryParticleConfig defConfig, InventoryParticleConfig config) {
		return SimpleGroup.startBuilder("visual").options(
				SimpleOption.<ParticlesDeletionMode>startBuilder("particles_deletion_mode")
						.withBinding(defConfig.getParticlesDeletionMode(), config::getParticlesDeletionMode, config::setParticlesDeletionMode, true)
						.withController(ParticlesDeletionMode.class)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Integer>startBuilder("particles_count_limit")
						.withBinding(defConfig.getParticlesCountLimit(), config::getParticlesCountLimit, config::setParticlesCountLimit, true)
						.withController(0, Integer.MAX_VALUE, 1, false)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Double>startBuilder("particles_transparency")
						.withBinding(defConfig.getParticleTransparency(), config::getParticleTransparency, config::setParticleTransparency, true)
						.withController(0.0D, 1.0D, 0.05D)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Integer>startBuilder("fade_out_duration")
						.withBinding(defConfig.getFadeOutDurationTicks(), config::getFadeOutDurationTicks, config::setFadeOutDurationTicks, true)
						.withController(0, 40, 1, true)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID)
		);
	}

}
