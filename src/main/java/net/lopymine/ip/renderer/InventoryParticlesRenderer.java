package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.element.mod.*;
import net.lopymine.ip.element.mod.spawner.IParticleSpawner;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.lopymine.ip.utils.NativeImageUtils;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class InventoryParticlesRenderer extends AbstractInventoryElementsRenderer<InventoryParticle> {

	private static final InventoryParticlesRenderer INSTANCE = new InventoryParticlesRenderer();

	private InventoryCursor cursor = new InventoryCursor();

	private InventoryParticlesRenderer() { }

	public static InventoryParticlesRenderer getInstance() {
		return INSTANCE;
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryParticles.LOGGER;
	}

	@Override
	protected String getModName() {
		return InventoryParticles.MOD_NAME;
	}

	public void init() {
		this.cursor = new InventoryCursor();
		double mouseX = Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
		double mouseY = Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();

		this.cursor.setMouseX(mouseX);
		this.cursor.setX(mouseX);
		this.cursor.setLastX(mouseX);

		this.cursor.setMouseY(mouseY);
		this.cursor.setY(mouseY);
		this.cursor.setLastY(mouseY);

		super.init();
	}

	@Override
	protected void renderElements(GuiGraphicsExtractor context, float tickProgress) {
		for (InventoryParticle particle : this.getScreenElements()) {
			if (particle == null) {
				continue;
			}
			particle.render(context, this.cursor, tickProgress, this.isStoppedTicking());
			if (particle.isHovered()) {
				this.setHoveredElement(particle);
			}
		}
		OptimizedDrawer.endDrawing();
	}

	public void tick(@Nullable AbstractContainerMenu handler, @Nullable Integer inventoryX, @Nullable Integer inventoryY) {
		if (!this.shouldTick()){
			return;
		}

		this.runSoft(() -> {
			this.cursor.tick();

			InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
			if (config.isGuiSlotsSpawnEnabled() && inventoryX != null && inventoryY != null && handler != null) {
				this.spawnAllSlotsParticles(handler, inventoryX, inventoryY);
			}
			if (config.isHoveredSlotSpawnEnabled() && inventoryX != null && inventoryY != null) {
				this.spawnHoveredSlotParticles(inventoryX, inventoryY);
			}
			if (config.isCursorSpawnEnabled()) {
				this.spawnCursorParticles();
			}

			this.pushPendingElements();
			this.tickElements();
		}, "ticking_inventory_particles");
	}

	private void spawnHoveredSlotParticles(int inventoryX, int inventoryY) {
		Slot hoveredSlot = this.cursor.getHoveredSlot();
		if (hoveredSlot == null) {
			return;
		}
		ItemStack stack = hoveredSlot.getItem();
		if (stack.isEmpty()) {
			return;
		}
		Item item = stack.getItem();
		List<IParticleSpawner> spawners = ParticlesConfigsManager.getSpawnersForItem(item);
		if (spawners == null) {
			return;
		}
		ParticleSpawnContext context = ParticleSpawnContext.hoveredSlot(hoveredSlot, inventoryX, inventoryY);
		for (IParticleSpawner spawner : spawners) {
			for (InventoryParticle particle : spawner.tickAndSpawn(context)) {
				this.addPendingElement(particle);
			}
		}
	}

	private void spawnAllSlotsParticles(AbstractContainerMenu handler, int inventoryX, int inventoryY) {
		for (Slot slot : handler.slots) {
			if (this.cursor.getHoveredSlot() != null && this.cursor.getHoveredSlot().index == slot.index) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (stack.isEmpty()) {
				continue;
			}
			List<IParticleSpawner> particleSpawners = ParticlesConfigsManager.getSpawnersForItem(stack.getItem());
			if (particleSpawners == null) {
				continue;
			}
			ParticleSpawnContext context = ParticleSpawnContext.slots(slot, inventoryX, inventoryY);
			for (IParticleSpawner spawner : particleSpawners) {
				for (InventoryParticle particle : spawner.tickAndSpawn(context)) {
					this.addPendingElement(particle);
				}
			}
		}
	}

	private void spawnCursorParticles() {
		ItemStack stack = this.cursor.getCurrentStack();
		if (stack.isEmpty()) {
			return;
		}
		List<IParticleSpawner> particleSpawners = ParticlesConfigsManager.getSpawnersForItem(stack.getItem());
		if (particleSpawners != null) {
			List<InventoryParticle> particles = new ArrayList<>();

			for (IParticleSpawner spawner : particleSpawners) {
				particles.addAll(spawner.tickAndSpawn(ParticleSpawnContext.cursor(this.cursor)));
				particles.addAll(spawner.spawnFromCursor(this.cursor));
			}

			particles.forEach(this::addPendingElement);
		}
	}

	public void onGuiAction(Slot slot, ItemStack stack, int inventoryX, int inventoryY) {
		if (stack.isEmpty() && !slot.hasItem()){
			return;
		}
		int chanceOfSpawn = 100 - (int) Math.ceil(InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGuiActionConfig().getCooldownCoefficient());
		int r = this.getRandom().nextIntBetweenInclusive(0, 100);
		if (r < chanceOfSpawn) {
			return;
		}
		this.runSoft(() -> {
			List<IParticleSpawner> spawners = ParticlesConfigsManager.getSpawnersForItem(stack.isEmpty() ? slot.getItem().getItem() : stack.getItem());
			if (spawners != null) {
				ParticleSpawnContext context = ParticleSpawnContext.guiActionSlot(slot, inventoryX, inventoryY);
				if (context.getStack().isEmpty()) {
					context.setStack(stack);
				}
				for (IParticleSpawner spawner : spawners) {
					for (InventoryParticle particle : spawner.spawn(context)) {
						this.addPendingElement(particle);
					}
				}
			}
		}, "put_in_slot");
	}
}
