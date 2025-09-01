package net.lopymine.ip.yacl;

import dev.isxander.yacl3.api.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.spawn.ParticleSpawnType;
import net.lopymine.ip.yacl.utils.SimpleContent;
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
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Boolean>startBuilder("debug_mode_enabled")
						.withBinding(defConfig.isDebugModeEnabled(), config::isDebugModeEnabled, config::setDebugModeEnabled, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Boolean>startBuilder("nbt_debug_mode_enabled")
						.withBinding(defConfig.isNbtDebugModeEnabled(), config::isNbtDebugModeEnabled, config::setNbtDebugModeEnabled, false)
						.withController(ENABLED_OR_DISABLE_FORMATTER)
						.withDescription(SimpleContent.NONE)
						.build()
		).build();
	}

	private static OptionGroup getParticlesGroup(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleGroup.startBuilder("particles_group").options(
				SimpleOption.<ParticleSpawnType>startBuilder("particle_spawn_type")
						.withBinding(defConfig.getParticleSpawnType(), config::getParticleSpawnType, config::setParticleSpawnType, true)
						.withController(ParticleSpawnType.class)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("global_particles_spawn_count_coefficient")
						.withBinding(defConfig.getGlobalParticlesSpawnCountCoefficient(), config::getGlobalParticlesSpawnCountCoefficient, config::setGlobalParticlesSpawnCountCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("global_particles_spawn_range_coefficient")
						.withBinding(defConfig.getGlobalParticlesSpawnRangeCoefficient(), config::getGlobalParticlesSpawnRangeCoefficient, config::setGlobalParticlesSpawnRangeCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("cursor_particles_spawn_count_coefficient")
						.withBinding(defConfig.getCursorParticlesSpawnCountCoefficient(), config::getCursorParticlesSpawnCountCoefficient, config::setCursorParticlesSpawnCountCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("cursor_particles_spawn_range_coefficient")
						.withBinding(defConfig.getCursorParticlesSpawnRangeCoefficient(), config::getCursorParticlesSpawnRangeCoefficient, config::setCursorParticlesSpawnRangeCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("gui_particles_spawn_count_coefficient")
						.withBinding(defConfig.getGuiParticlesSpawnCountCoefficient(), config::getGuiParticlesSpawnCountCoefficient, config::setGuiParticlesSpawnCountCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("gui_particles_spawn_range_coefficient")
						.withBinding(defConfig.getGuiParticlesSpawnRangeCoefficient(), config::getGuiParticlesSpawnRangeCoefficient, config::setGuiParticlesSpawnRangeCoefficient, true)
						.withController(0.0F, 2.0F, 0.1F)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Integer>startBuilder("maximum_particles_limit")
						.withBinding(defConfig.getMaxParticles(), config::getMaxParticles, config::setMaxParticles, true)
						.withController(0, Integer.MAX_VALUE, 1, false)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<ParticleDeletionMode>startBuilder("particle_deletion_mode")
						.withBinding(defConfig.getParticleDeletionMode(), config::getParticleDeletionMode, config::setParticleDeletionMode, true)
						.withController(ParticleDeletionMode.class)
						.withDescription(SimpleContent.NONE)
						.build()
		).build();
	}

}


