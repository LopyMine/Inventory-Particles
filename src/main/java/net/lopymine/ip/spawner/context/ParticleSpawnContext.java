package net.lopymine.ip.spawner.context;

import java.util.function.*;
import lombok.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.*;
import net.lopymine.ip.config.sub.InventoryParticlesCoefficientsConfig.ParticleCoefficientConfig;
import net.lopymine.ip.element.InventoryCursor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class ParticleSpawnContext {

	public static final ParticleSpawnContext CURSOR_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isCursorSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getCursorConfig
	);

	public static final ParticleSpawnContext HOVERED_SLOT_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isHoveredSlotSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getHoveredSlotConfig
	);

	public static final ParticleSpawnContext ALL_SLOTS_CONTEXT = new ParticleSpawnContext(
			InventoryParticleConfig::isGuiSlotsSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getAllSlotsConfig
	);

	public static final ParticleSpawnContext GUI_ACTION_SLOT = new ParticleSpawnContext(
			InventoryParticleConfig::isGuiActionSpawnEnabled,
			InventoryParticlesCoefficientsConfig::getGuiActionConfig,
			ParticleCoefficientConfig::getCountCoefficient,
			(config) -> 1.0D
	);

	private ItemStack stack;
	private int x, y;
	private double impulseX, impulseY;
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

	public static ParticleSpawnContext hoveredSlot(Slot slot, int inventoryX, int inventoryY) {
		return slot(HOVERED_SLOT_CONTEXT, slot, inventoryX, inventoryY);
	}

	public static ParticleSpawnContext guiActionSlot(Slot slot, int inventoryX, int inventoryY) {
		return slot(GUI_ACTION_SLOT, slot, inventoryX, inventoryY);
	}

	public static ParticleSpawnContext slots(Slot slot, int inventoryX, int inventoryY) {
		return slot(ALL_SLOTS_CONTEXT, slot, inventoryX, inventoryY);
	}

	private static @NotNull ParticleSpawnContext slot(ParticleSpawnContext context, Slot slot, int inventoryX, int inventoryY) {
		context.setStack(slot.getStack());
		context.setX(inventoryX + slot.x + 8);
		context.setY(inventoryY + slot.y + 8);
		context.setImpulseX(0F);
		context.setImpulseY(0F);
		return context;
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
