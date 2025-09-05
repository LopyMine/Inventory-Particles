package net.lopymine.ip.spawner.context;

import java.util.function.*;
import lombok.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.*;
import net.lopymine.ip.config.sub.InventoryParticlesCoefficientsConfig.ParticleCoefficientConfig;
import net.lopymine.ip.element.InventoryCursor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Getter
@Setter
public class ParticleSpawnContext {

	private static final ParticleSpawnContext CURSOR_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isCursorSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getCursorConfig
	);

	private static final ParticleSpawnContext HOVERED_SLOT_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isHoveredSlotSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getHoveredSlotConfig
	);

	private static final ParticleSpawnContext ALL_SLOTS_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isGuiSlotsSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getAllSlotsConfig
	);

	private static final ParticleSpawnContext GUI_ACTION_SLOT = new ParticleSpawnContext(
			InventoryParticleConfig::isGuiActionSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getGuiActionConfig,
			ParticleCoefficientConfig::getCountCoefficient,
			(config) -> 1.0D
	);

	private ItemStack stack;
	private int x, y;
	private float impulseX, impulseY;
	private Function<InventoryParticleConfig, Boolean> enabledFunction;
	private Function<InventoryParticlesCoefficientsConfig, ParticleCoefficientConfig> configFunction;
	private Function<ParticleCoefficientConfig, Double> countFunction;
	private Function<ParticleCoefficientConfig, Double> cooldownFunction;

	public ParticleSpawnContext(Function<InventoryParticleConfig, Boolean> enabledFunction, Function<InventoryParticlesCoefficientsConfig, ParticleCoefficientConfig> configFunction) {
		this(enabledFunction, configFunction, ParticleCoefficientConfig::getCountCoefficient, ParticleCoefficientConfig::getCooldownCoefficient);
	}

	public ParticleSpawnContext(Function<InventoryParticleConfig, Boolean> enabledFunction, Function<InventoryParticlesCoefficientsConfig, ParticleCoefficientConfig> configFunction, Function<ParticleCoefficientConfig, Double> countFunction, Function<ParticleCoefficientConfig, Double> cooldownFunction) {
		this.enabledFunction  = enabledFunction;
		this.configFunction   = configFunction;
		this.countFunction    = countFunction;
		this.cooldownFunction = cooldownFunction;
	}

	public static ParticleSpawnContext cursor(InventoryCursor cursor) {
		CURSOR_CONTEXT.setStack(cursor.getCurrentStack());
		CURSOR_CONTEXT.setX(cursor.getX());
		CURSOR_CONTEXT.setY(cursor.getY());
		CURSOR_CONTEXT.setImpulseX(cursor.getSpeedX());
		CURSOR_CONTEXT.setImpulseY(cursor.getSpeedY());
		return CURSOR_CONTEXT;
	}

	public static ParticleSpawnContext slot(Slot slot, int inventoryX, int inventoryY) {
		ALL_SLOTS_CONTEXT.setStack(slot.getStack());
		ALL_SLOTS_CONTEXT.setX(inventoryX + slot.x + 8);
		ALL_SLOTS_CONTEXT.setY(inventoryY + slot.y + 8);
		ALL_SLOTS_CONTEXT.setImpulseX(0F);
		ALL_SLOTS_CONTEXT.setImpulseY(0F);
		return ALL_SLOTS_CONTEXT;
	}

	public static ParticleSpawnContext guiActionSlot(Slot slot, int inventoryX, int inventoryY) {
		GUI_ACTION_SLOT.setStack(slot.getStack());
		GUI_ACTION_SLOT.setX(inventoryX + slot.x + 8);
		GUI_ACTION_SLOT.setY(inventoryY + slot.y + 8);
		GUI_ACTION_SLOT.setImpulseX(0F);
		GUI_ACTION_SLOT.setImpulseY(0F);
		return GUI_ACTION_SLOT;
	}

	public static ParticleSpawnContext hoveredSlot(Slot slot, int inventoryX, int inventoryY) {
		HOVERED_SLOT_CONTEXT.setStack(slot.getStack());
		HOVERED_SLOT_CONTEXT.setX(inventoryX + slot.x + 8);
		HOVERED_SLOT_CONTEXT.setY(inventoryY + slot.y + 8);
		HOVERED_SLOT_CONTEXT.setImpulseX(0F);
		HOVERED_SLOT_CONTEXT.setImpulseY(0F);
		return HOVERED_SLOT_CONTEXT;
	}

	public double getCountCoefficient() {
		InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
		Boolean enabled = this.enabledFunction.apply(config);
		if (!enabled) {
			return 1.0F;
		}
		return this.countFunction.apply(this.configFunction.apply(InventoryParticlesConfig.getInstance().getCoefficientsConfig()));
	}

	public double getCooldownCoefficient() {
		Boolean enabled = this.enabledFunction.apply(InventoryParticlesConfig.getInstance().getParticleConfig());
		if (!enabled) {
			return 1.0F;
		}
		return this.cooldownFunction.apply(this.configFunction.apply(InventoryParticlesConfig.getInstance().getCoefficientsConfig()));
	}
}
