package net.lopymine.ip.yacl.category;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticlesDeletionMode;
import net.lopymine.ip.config.sub.*;
import net.lopymine.ip.config.sub.InventoryParticlesCacheConfig.CacheInvalidateMode;
import net.lopymine.ip.config.sub.InventoryParticlesFamilyGenerationConfig.Mode;
import net.lopymine.ip.family.cache.FamilyParticlesCacheManager;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;

@ExtensionMethod(SimpleOptionExtension.class)
public class GeneralCategory {

	public static SimpleCategory get(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		return SimpleCategory.startBuilder("general")
				.groups(getMainGroup(defConfig.getMainConfig(), config.getMainConfig()))
				.groups(getVisualGroup(defConfig.getParticleConfig(), config.getParticleConfig()))
				.groups(getFamilyGenerationGroup(defConfig.getFamilyGenerationConfig(), config.getFamilyGenerationConfig()))
				.groups(getCacheGroup(defConfig.getCacheConfig(), config.getCacheConfig()));
	}

	private static SimpleGroup getFamilyGenerationGroup(InventoryParticlesFamilyGenerationConfig defConfig, InventoryParticlesFamilyGenerationConfig config) {
		return SimpleGroup.startBuilder("family_generation").options(
				SimpleOption.<Mode>startBuilder("family_generation_mods_mode")
						.withBinding(defConfig.getModsMode(), config::getModsMode, config::setModsMode, true)
						.withController(Mode.class)
						.withDescription(SimpleContent.NONE),
				SimpleOption.<String>startBuilder("family_generation_mods")
						.withBinding(defConfig.getMods(), config::getMods, config::setMods, true)
						.withController()
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Mode>startBuilder("family_generation_items_mode")
						.withBinding(defConfig.getItemsMode(), config::getItemsMode, config::setItemsMode, true)
						.withController(Mode.class)
						.withDescription(SimpleContent.NONE),
				SimpleOption.<String>startBuilder("family_generation_items")
						.withBinding(defConfig.getItems(), config::getItems, config::setItems, true)
						.withController()
						.withDescription(SimpleContent.NONE)
		);
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
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Integer>startBuilder("particles_count_limit")
						.withBinding(defConfig.getParticlesCountLimit(), config::getParticlesCountLimit, config::setParticlesCountLimit, true)
						.withController(0, Integer.MAX_VALUE, 1, false)
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Double>startBuilder("particles_transparency")
						.withBinding(defConfig.getParticleTransparency(), config::getParticleTransparency, config::setParticleTransparency, true)
						.withController(0.0D, 1.0D, 0.05D)
						.withDescription(SimpleContent.NONE),
				SimpleOption.<Integer>startBuilder("fade_out_duration")
						.withBinding(defConfig.getFadeOutDurationTicks(), config::getFadeOutDurationTicks, config::setFadeOutDurationTicks, true)
						.withController(0, 40, 1, true)
						.withDescription(SimpleContent.NONE)
		);
	}

	private static SimpleGroup getCacheGroup(InventoryParticlesCacheConfig defConfig, InventoryParticlesCacheConfig config) {
		return SimpleGroup.startBuilder("cache").options(
				SimpleOption.<CacheInvalidateMode>startBuilder("invalidate_mode")
					.withBinding(defConfig.getInvalidateMode(), config::getInvalidateMode, config::setInvalidateMode, true)
					.withController(CacheInvalidateMode.class)
					.withDescription(SimpleContent.NONE),
				SimpleOption.startButtonBuilder("invalidate_now", (screen, option) -> {
					FamilyParticlesCacheManager.deleteSilence();
					}).withDescription(SimpleContent.NONE)
		);
	}

}
