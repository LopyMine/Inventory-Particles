package net.lopymine.ip.yacl;

import dev.isxander.yacl3.api.*;
import java.util.function.Function;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.sub.*;
import net.lopymine.ip.config.sub.InventoryParticlesCoefficientsConfig.ParticleCoefficientConfig;
import net.lopymine.ip.utils.ModMenuUtils;
import net.lopymine.ip.yacl.base.*;
import net.lopymine.ip.yacl.extension.SimpleOptionExtension;
import net.lopymine.ip.yacl.screen.SimpleYACLScreen;
import net.lopymine.ip.yacl.utils.SimpleContent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
				.groups(getMainGroup(defConfig.getMainConfig(), config.getMainConfig()))
				.groups(getParticlesGroups(defConfig, config))
				.build();
	}

	private static OptionGroup getMainGroup(InventoryParticlesMainConfig defConfig, InventoryParticlesMainConfig config) {
		return SimpleGroup.startBuilder("main").options(
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

	private static OptionGroup[] getParticlesGroups(InventoryParticlesConfig defModConfig, InventoryParticlesConfig modConfig) {
		InventoryParticlesCoefficientsConfig defCoefficientConfig = defModConfig.getCoefficientsConfig();
		InventoryParticlesCoefficientsConfig coefficientConfig = modConfig.getCoefficientsConfig();

		//
		SimpleGroup coefficientsGroup = SimpleGroup.startBuilder("coefficients");
		createCoefficientsConfig(coefficientsGroup, "global", defCoefficientConfig.getGlobalConfig(), coefficientConfig.getGlobalConfig());
		Option<?>[] cursorOptions = createCoefficientsConfig(coefficientsGroup, "cursor", defCoefficientConfig.getCursorConfig(), coefficientConfig.getCursorConfig());
		Option<?>[] hoveredSlotOptions = createCoefficientsConfig(coefficientsGroup, "hovered_slot", defCoefficientConfig.getHoveredSlotConfig(), coefficientConfig.getHoveredSlotConfig());
		Option<?>[] guiSlotsOptions = createCoefficientsConfig(coefficientsGroup, "gui_slots", defCoefficientConfig.getAllSlotsConfig(), coefficientConfig.getAllSlotsConfig());
		Option<?>[] guiActionOptions = {
				SimpleOption.<Double>startBuilder("gui_action_count_coefficient")
						.withBinding(defCoefficientConfig.getGuiActionConfig().getCountCoefficient(), coefficientConfig.getGuiActionConfig()::getCountCoefficient, coefficientConfig.getGuiActionConfig()::setCountCoefficient, true)
						.withController(0.0D, 50D, 0.1D)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Double>startBuilder("gui_action_spawn_chance")
						.withBinding(defCoefficientConfig.getGuiActionConfig().getCooldownCoefficient(), coefficientConfig.getGuiActionConfig()::getCooldownCoefficient, coefficientConfig.getGuiActionConfig()::setCooldownCoefficient, true)
						.withController(0.0D, 100D, 0.1D)
						.withDescription(SimpleContent.NONE)
						.build()};
		coefficientsGroup.options(guiActionOptions);

		//
		InventoryParticleConfig defParticleConfig = defModConfig.getParticleConfig();
		InventoryParticleConfig particleConfig = modConfig.getParticleConfig();

		Option<Boolean> guiActionTakeSpawnEnabled = SimpleOption.<Boolean>startBuilder("gui_action_take_spawn_enabled")
				.withBinding(defParticleConfig.isGuiActionTakeSpawnEnabled(), particleConfig::isGuiActionTakeSpawnEnabled, particleConfig::setGuiActionTakeSpawnEnabled, true)
				.withController()
				.withDescription(SimpleContent.NONE)
				.build();
		Option<Boolean> guiActionPutSpawnEnabled = SimpleOption.<Boolean>startBuilder("gui_action_put_spawn_enabled")
				.withBinding(defParticleConfig.isGuiActionPutSpawnEnabled(), particleConfig::isGuiActionPutSpawnEnabled, particleConfig::setGuiActionPutSpawnEnabled, true)
				.withController()
				.withDescription(SimpleContent.NONE)
				.build();

		//
		SimpleGroup particlesGroup = SimpleGroup.startBuilder("particles").options(
				SimpleOption.<Boolean>startBuilder("cursor_spawn_enabled")
						.withBinding(defParticleConfig.isCursorSpawnEnabled(), particleConfig::isCursorSpawnEnabled, particleConfig::setCursorSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.getOptionBuilder()
						.addListener((o, e) -> {
							for (Option<?> option : cursorOptions) {
								option.setAvailable(o.pendingValue());
							}
						})
						.build(),
				SimpleOption.<Boolean>startBuilder("hovered_slot_spawn_enabled")
						.withBinding(defParticleConfig.isHoveredSlotSpawnEnabled(), particleConfig::isHoveredSlotSpawnEnabled, particleConfig::setHoveredSlotSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.getOptionBuilder()
						.addListener((o, e) -> {
							for (Option<?> option : hoveredSlotOptions) {
								option.setAvailable(o.pendingValue());
							}
						})
						.build(),
				SimpleOption.<Boolean>startBuilder("gui_slots_spawn_enabled")
						.withBinding(defParticleConfig.isGuiSlotsSpawnEnabled(), particleConfig::isGuiSlotsSpawnEnabled, particleConfig::setGuiSlotsSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.getOptionBuilder()
						.addListener((o, e) -> {
							for (Option<?> option : guiSlotsOptions) {
								option.setAvailable(o.pendingValue());
							}
						})
						.build(),
				SimpleOption.<Boolean>startBuilder("gui_action_spawn_enabled")
						.withBinding(defParticleConfig.isGuiActionSpawnEnabled(), particleConfig::isGuiActionSpawnEnabled, particleConfig::setGuiActionSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.getOptionBuilder()
						.addListener((o, e) -> {
							boolean value = o.pendingValue();
							for (Option<?> option : guiActionOptions) {
								option.setAvailable(value);
							}
							guiActionTakeSpawnEnabled.setAvailable(value);
							guiActionPutSpawnEnabled.setAvailable(value);

							if (!value) {
								guiActionTakeSpawnEnabled.requestSet(false);
								guiActionPutSpawnEnabled.requestSet(false);
							}
						})
						.build(),
				guiActionTakeSpawnEnabled,
				guiActionPutSpawnEnabled,
				SimpleOption.<ParticleDeletionMode>startBuilder("particle_deletion_mode")
						.withBinding(defParticleConfig.getParticleDeletionMode(), particleConfig::getParticleDeletionMode, particleConfig::setParticleDeletionMode, true)
						.withController(ParticleDeletionMode.class)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Integer>startBuilder("max_particles")
						.withBinding(defParticleConfig.getMaxParticles(), particleConfig::getMaxParticles, particleConfig::setMaxParticles, true)
						.withController(0, Integer.MAX_VALUE, 1, false)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Float>startBuilder("particle_transparency")
						.withBinding(defParticleConfig.getParticleTransparency(), particleConfig::getParticleTransparency, particleConfig::setParticleTransparency, true)
						.withController(0.0F, 1.0F, 0.05F)
						.withDescription(SimpleContent.NONE)
						.build()
		);

		return new OptionGroup[]{particlesGroup.build(), coefficientsGroup.build()};
	}

	private static Option<?>[] createCoefficientsConfig(SimpleGroup group, String id, ParticleCoefficientConfig defConfig, ParticleCoefficientConfig config) {
		Option<?>[] options = new Option[]{
				SimpleOption.<Double>startBuilder(id + "_count_coefficient")
						.withBinding(defConfig.getCountCoefficient(), config::getCountCoefficient, config::setCountCoefficient, true)
						.withController(0.0D, 50D, 0.1D)
						.withDescription(SimpleContent.NONE)
						.build(),
				SimpleOption.<Double>startBuilder(id + "_cooldown_coefficient")
						.withBinding(defConfig.getCooldownCoefficient(), config::getCooldownCoefficient, config::setCooldownCoefficient, true)
						.withController(0.0D, 50D, 0.1D)
						.withDescription(SimpleContent.NONE)
						.build()
		};
		group.options(options);
		return options;
	}

}


