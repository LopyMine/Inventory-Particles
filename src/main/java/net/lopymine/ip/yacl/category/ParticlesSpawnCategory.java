package net.lopymine.ip.yacl.category;

import dev.isxander.yacl3.api.Option;
import java.util.Arrays;
import java.util.function.*;
import java.util.stream.Stream;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.*;
import net.lopymine.ip.config.sub.InventoryParticlesCoefficientsConfig.ParticleCoefficientConfig;
import net.lopymine.ip.config.sub.InventoryParticlesItemWhitelistsConfig.*;
import net.lopymine.mossylib.yacl.api.*;
import net.lopymine.mossylib.yacl.extension.SimpleOptionExtension;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(SimpleOptionExtension.class)
public class ParticlesSpawnCategory {

	public static SimpleCategory get(InventoryParticlesConfig defConfig, InventoryParticlesConfig config) {
		InventoryParticlesCoefficientsConfig dc = defConfig.getCoefficientsConfig();
		InventoryParticleConfig dp = defConfig.getParticleConfig();
		InventoryParticlesItemWhitelistsConfig dw = defConfig.getWhitelistsConfig();

		InventoryParticlesCoefficientsConfig c = config.getCoefficientsConfig();
		InventoryParticleConfig p = config.getParticleConfig();
		InventoryParticlesItemWhitelistsConfig w = config.getWhitelistsConfig();

		SimpleCategory category = SimpleCategory.startBuilder("particles_spawn");

		createSection(
				category,
				"global",
				dc.getGlobalConfig(), c.getGlobalConfig(),
				null, null,
				null, null, null
		);

		createSection(
				category,
				"cursor",
				dc.getCursorConfig(), c.getCursorConfig(),
				dw.getCursorConfig(), w.getCursorConfig(),
				dp.isCursorSpawnEnabled(), p::isCursorSpawnEnabled, p::setCursorSpawnEnabled
		);

		createSection(
				category,
				"hovered_slot",
				dc.getHoveredSlotConfig(), c.getHoveredSlotConfig(),
				dw.getHoveredSlotConfig(), w.getHoveredSlotConfig(),
				dp.isHoveredSlotSpawnEnabled(), p::isHoveredSlotSpawnEnabled, p::setHoveredSlotSpawnEnabled
		);

		createSection(
				category,
				"gui_slots",
				dc.getGuiSlotsConfig(), c.getGuiSlotsConfig(),
				dw.getGuiSlotsConfig(), w.getGuiSlotsConfig(),
				dp.isGuiSlotsSpawnEnabled(), p::isGuiSlotsSpawnEnabled, p::setGuiSlotsSpawnEnabled
		);

		createSection(
				category,
				"gui_actions",
				dp.isGuiActionsSpawnEnabled(), p::isGuiActionsSpawnEnabled, p::setGuiActionsSpawnEnabled,
				getGuiActionToggleOptions(dp, p),
				getGuiActionCoefficientOptions(dc, c, dp, p),
				createWhitelistOptions(dw.getGuiActionConfig(), w.getGuiActionConfig())
		);

		return category;
	}

	public static void createSection(
			SimpleCategory category,
			String id,
			ParticleCoefficientConfig defConfig, ParticleCoefficientConfig config,
			@Nullable ParticlesItemWhitelistConfig defWhitelistConfig, @Nullable ParticlesItemWhitelistConfig whitelistConfig,
			@Nullable Boolean defValue, @Nullable Supplier<Boolean> getter, @Nullable Consumer<Boolean> setter
	) {

		Option<?>[] coefficientsOptions = createCoefficientsOptions(defConfig, config);
		Option<?>[] whitelistOptions = defWhitelistConfig != null && whitelistConfig != null
				? createWhitelistOptions(defWhitelistConfig, whitelistConfig) : new Option[0];

		createSection(category, id, defValue, getter, setter, coefficientsOptions, whitelistOptions);
	}

	public static void createSection(
			SimpleCategory category,
			String id,
			@Nullable Boolean defValue, @Nullable Supplier<Boolean> getter, @Nullable Consumer<Boolean> setter,
			Option<?>[]... options
	) {
		createSection(category, id, defValue, getter, setter, Stream.of(options).flatMap(Arrays::stream).toArray(Option[]::new));
	}

	public static void createSection(
			SimpleCategory category,
			String id,
			@Nullable Boolean defValue, @Nullable Supplier<Boolean> getter, @Nullable Consumer<Boolean> setter,
			Option<?>... options
	) {

		Option<Boolean> option = defValue != null && getter != null && setter != null
				? createToggle(defValue, getter, setter, options) : null;

		createSection(category, id, option, options);
	}

	public static void createSection(
			SimpleCategory category,
			String id,
			@Nullable Option<?> toggleOption,
			Option<?>... options
	) {
		SimpleGroup group = SimpleGroup.startBuilder(id + "_section");

		if (toggleOption != null) {
			group.options(toggleOption);
		}
		if (options.length > 0) {
			group.options(options);
		}

		category.groups(group);
	}

	private static Option<?>[] getGuiActionCoefficientOptions(InventoryParticlesCoefficientsConfig defCoefficientConfig, InventoryParticlesCoefficientsConfig coefficientConfig, InventoryParticleConfig defParticleConfig, InventoryParticleConfig particleConfig) {
		return new Option[] {
				SimpleOption.<Double>startBuilder("count_coefficient")
						.withBinding(defCoefficientConfig.getGuiActionConfig().getCountCoefficient(), coefficientConfig.getGuiActionConfig()::getCountCoefficient, coefficientConfig.getGuiActionConfig()::setCountCoefficient, true)
						.withController(0.0D, 50D, 0.1D, false)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Double>startBuilder("spawn_chance")
						.withBinding(defCoefficientConfig.getGuiActionConfig().getCooldownCoefficient(), coefficientConfig.getGuiActionConfig()::getCooldownCoefficient, coefficientConfig.getGuiActionConfig()::setCooldownCoefficient, true)
						.withController(0.0D, 100D, 0.1D, false)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID)
		};
	}

	private static Option<?>[] getGuiActionToggleOptions(InventoryParticleConfig defParticleConfig, InventoryParticleConfig particleConfig) {
		return new Option[] {
				SimpleOption.<Boolean>startBuilder("gui_action_take_spawn_enabled")
						.withBinding(defParticleConfig.isGuiActionTakeSpawnEnabled(), particleConfig::isGuiActionTakeSpawnEnabled, particleConfig::setGuiActionTakeSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Boolean>startBuilder("gui_action_put_spawn_enabled")
						.withBinding(defParticleConfig.isGuiActionPutSpawnEnabled(), particleConfig::isGuiActionPutSpawnEnabled, particleConfig::setGuiActionPutSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Boolean>startBuilder("gui_action_quick_move_spawn_enabled")
						.withBinding(defParticleConfig.isGuiActionQuickMoveSpawnEnabled(), particleConfig::isGuiActionQuickMoveSpawnEnabled, particleConfig::setGuiActionQuickMoveSpawnEnabled, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID)
		};
	}

	private static Option<Boolean> createToggle(boolean defValue, Supplier<Boolean> getter, Consumer<Boolean> setter, Option<?>... dependents) {
		var builder = SimpleOption.<Boolean>startBuilder("spawn_enabled")
				.withBinding(defValue, getter, setter, true)
				.withController()
				.withDescription(SimpleContent.NONE);

		if (dependents != null && dependents.length > 0) {
			builder.custom((b) -> {
				b.addListener((o, e) -> {
					boolean value = Boolean.TRUE.equals(getter.get());
					for (Option<?> option : dependents) {
						option.setAvailable(value);
					}
				});
			});
		}

		return builder.build(InventoryParticles.MOD_ID);
	}

	private static Option<?>[] createCoefficientsOptions(ParticleCoefficientConfig defConfig, ParticleCoefficientConfig config) {
		return new Option[]{
				SimpleOption.<Double>startBuilder("count_coefficient")
						.withBinding(defConfig.getCountCoefficient(), config::getCountCoefficient, config::setCountCoefficient, true)
						.withController(0.0D, 50D, 0.1D, false)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<Double>startBuilder("cooldown_coefficient")
						.withBinding(defConfig.getCooldownCoefficient(), config::getCooldownCoefficient, config::setCooldownCoefficient, true)
						.withController(0.0D, 50D, 0.1D, false)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID)
		};
	}

	private static Option<?>[] createWhitelistOptions(ParticlesItemWhitelistConfig defConfig, ParticlesItemWhitelistConfig config) {
		return new Option[] {
				SimpleOption.<Mode>startBuilder("whitelist_mode")
						.withBinding(defConfig.getMode(), config::getMode, config::setMode, true)
						.withController(Mode.class)
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID),
				SimpleOption.<String>startBuilder("whitelist_items")
						.withBinding(defConfig.getItems(), config::getItems, config::setItems, true)
						.withController()
						.withDescription(SimpleContent.NONE)
						.build(InventoryParticles.MOD_ID)
		};
	}

}
