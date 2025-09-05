package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.element.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.resourcepack.ResourcePackParticleConfigsManager;
import net.lopymine.ip.spawner.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class InventoryParticlesRenderer extends TickElement {

	private static final InventoryParticlesRenderer INSTANCE = new InventoryParticlesRenderer();

	@Getter
	private final Collection<InventoryParticle> screenParticles = getScreenParticlesList();
	private final List<InventoryParticle> pendingParticles = new ArrayList<>();

	private InventoryCursor cursor = new InventoryCursor();

	private final Random random = Random.create();

	private boolean stoppedByInitializationReason;
	private boolean stopTicking;
	private int ticksPerTick = 1;
	private int nextTick = 1;

	@Nullable
	private InventoryParticle hoveredParticle;
	@Nullable
	private InventoryParticle selectedParticle;

	private InventoryParticlesRenderer() {
	}

	public static InventoryParticlesRenderer getInstance() {
		return INSTANCE;
	}

	public void render(DrawContext context, float tickProgress) {
		this.hoveredParticle = null;
		this.runSoft(() -> {
			for (InventoryParticle screenParticle : this.screenParticles) {
				if (screenParticle == null) {
					continue;
				}
				screenParticle.render(context, this.cursor, tickProgress);
				if (screenParticle.isHovered()) {
					this.hoveredParticle = screenParticle;
				}
			}
		}, "rendering_particle");
	}

	public void updateCursor(int mouseY, int mouseX, ItemStack item, Slot focusedSlot) {
		this.cursor.setMouseY(mouseY);
		this.cursor.setMouseX(mouseX);
		this.cursor.setCurrentStack(item);
		this.cursor.setHoveredSlot(focusedSlot);
	}

	public void tick(ScreenHandler handler, int inventoryX, int inventoryY) {
		if (this.stopTicking || this.stoppedByInitializationReason) {
			return;
		}
		super.tick();
		if (this.ticks < this.nextTick) {
			return;
		}
		this.nextTick = this.ticks + this.ticksPerTick;

		this.runSoft(() -> {
			this.cursor.tick();

			InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
			if (config.isGuiSlotsSpawnEnabled()) {
				this.spawnAllSlotsParticles(handler, inventoryX, inventoryY);
			}
			if (config.isHoveredSlotSpawnEnabled()) {
				this.spawnHoveredSlotParticles(inventoryX, inventoryY);
			}
			if (config.isCursorSpawnEnabled()) {
				this.spawnCursorParticles();
			}

			this.pushPendingParticles();

			this.screenParticles.removeIf((particle) -> {
				if (particle == null) {
					return true;
				}
				particle.tick();
				return particle.isDead() && !particle.isSelected();
			});
		}, "ticking_inventory_particles");
	}

	private void pushPendingParticles() {
		this.screenParticles.addAll(this.pendingParticles);
		this.pendingParticles.clear();
	}

	private void spawnHoveredSlotParticles(int inventoryX, int inventoryY) {
		if (false) {
			return;
		}
		Slot hoveredSlot = this.cursor.getHoveredSlot();
		if (hoveredSlot == null) {
			return;
		}
		ItemStack stack = hoveredSlot.getStack();
		if (stack.isEmpty()) {
			return;
		}
		Item item = stack.getItem();
		List<IParticleSpawner> spawners = ResourcePackParticleConfigsManager.getPerItemParticleSpawners().get(item);
		if (spawners == null) {
			return;
		}
		ParticleSpawnContext context = ParticleSpawnContext.hoveredSlot(hoveredSlot, inventoryX, inventoryY);
		for (IParticleSpawner spawner : spawners) {
			for (InventoryParticle particle : spawner.tickAndSpawn(context)) {
				this.spawnParticle(particle);
			}
		}
	}

	private void spawnAllSlotsParticles(ScreenHandler handler, int inventoryX, int inventoryY) {
		if (false) {
			return;
		}
		for (Slot slot : handler.slots) {
			if (this.cursor.getHoveredSlot() != null && this.cursor.getHoveredSlot().id == slot.id) {
				continue;
			}
			ItemStack stack = slot.getStack();
			if (stack.isEmpty()) {
				continue;
			}
			List<IParticleSpawner> particleSpawners = ResourcePackParticleConfigsManager.getPerItemParticleSpawners().get(stack.getItem());
			if (particleSpawners == null) {
				continue;
			}
			ParticleSpawnContext context = ParticleSpawnContext.slot(slot, inventoryX, inventoryY);
			for (IParticleSpawner spawner : particleSpawners) {
				for (InventoryParticle particle : spawner.tickAndSpawn(context)) {
					this.spawnParticle(particle);
				}
			}
		}
	}

	private void spawnCursorParticles() {
		if (false) {
			return;
		}
		Item currentItem = this.cursor.getCurrentStack().getItem();
		List<IParticleSpawner> particleSpawners = ResourcePackParticleConfigsManager.getPerItemParticleSpawners().get(currentItem);
		if (currentItem != Items.AIR && particleSpawners != null) {
			List<InventoryParticle> particles = new ArrayList<>();

			for (IParticleSpawner spawner : particleSpawners) {
				particles.addAll(spawner.tickAndSpawn(ParticleSpawnContext.cursor(this.cursor)));
				particles.addAll(spawner.spawnFromCursor(this.cursor));
			}

			particles.forEach(this::spawnParticle);
		}
	}

	private void spawnParticle(InventoryParticle particle) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		int difference = (this.screenParticles.size() + 1) - config.getParticleConfig().getMaxParticles();
		if (difference > 0) {
			this.clearParticlesForNewOnes(difference, config.getParticleConfig().getParticleDeletionMode());
		}
		this.pendingParticles.add(particle);
	}

	private void clearParticlesForNewOnes(int difference, ParticleDeletionMode mode) {
		switch (mode) {
			case OLDEST -> {
				if (!(this.screenParticles instanceof ArrayDeque<InventoryParticle> deque)) {
					return;
				}
				for (int i = 0; i < difference; i++) {
					deque.pollFirst();
				}
			}
			case RANDOM -> {
				if (!(this.screenParticles instanceof ArrayList<InventoryParticle> list)) {
					return;
				}
				for (int i = 0; i < difference; i++) {
					list.remove(this.random.nextBetween(0, list.size() - 1));
				}
			}
		}
	}

	public void clear() {
		this.screenParticles.clear();
		this.pendingParticles.clear();
		this.hoveredParticle = null;
		this.selectedParticle = null;
		this.stoppedByInitializationReason = true;
	}

	public void init() {
		this.cursor = new InventoryCursor();
		this.stoppedByInitializationReason = false;
	}

	public void mouseClicked(int button) {
		if (button != 1) {
			return;
		}

		if (!this.isStopTicking()) {
			return;
		}

		if (this.selectedParticle != null) {
			this.selectedParticle.setSelected(false);
			this.selectedParticle = null;
			ClickableWidget.playClickSound(MinecraftClient.getInstance().getSoundManager());
			return;
		}

		if (this.hoveredParticle == null) {
			return;
		}

		this.hoveredParticle.setSelected(true);
		this.selectedParticle = this.hoveredParticle;
		ClickableWidget.playClickSound(MinecraftClient.getInstance().getSoundManager());
	}

	public void updateParticlesPositions(float xCoefficient, float yCoefficient) {
		for (InventoryParticle particle : this.screenParticles) {
			particle.setX(particle.getX() * xCoefficient);
			particle.setY(particle.getY() * yCoefficient);
		}
		for (InventoryParticle particle : this.pendingParticles) {
			particle.setX(particle.getX() * xCoefficient);
			particle.setY(particle.getY() * yCoefficient);
		}
	}

	private void runSoft(Runnable runnable, String action) {
		try {
			runnable.run();
		} catch (Exception e) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player != null) {
				player.sendMessage(InventoryParticles.text("error." + action), false); // todo rework error message
			}
			InventoryParticlesClient.LOGGER.error("Failed to render particle!", e);
			InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
			config.getMainConfig().setModEnabled(false);
			config.saveAsync();
		}
	}

	public void onPutInSlot(Slot slot, ItemStack stack, int inventoryX, int inventoryY) {
		if (stack.isEmpty() && !slot.hasStack()){
			return;
		}
		int chanceOfSpawn = (int) (InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGuiActionConfig().getCooldownCoefficient() * 100F);
		if (this.random.nextBetween(0, 100) > chanceOfSpawn) {
			return;
		}
		this.runSoft(() -> {
			List<IParticleSpawner> spawners = ResourcePackParticleConfigsManager.getPerItemParticleSpawners().get(stack.isEmpty() ? slot.getStack().getItem() : stack.getItem());
			if (spawners != null) {
				ParticleSpawnContext context = ParticleSpawnContext.guiActionSlot(slot, inventoryX, inventoryY);
				if (context.getStack().isEmpty()) {
					context.setStack(stack);
				}
				for (IParticleSpawner spawner : spawners) {
					for (InventoryParticle particle : spawner.spawn(context)) {
						this.spawnParticle(particle);
					}
				}
			}
		}, "put_in_slot");
	}

	private static @NotNull Collection<InventoryParticle> getScreenParticlesList() {
		return switch (InventoryParticlesConfig.getInstance().getParticleConfig().getParticleDeletionMode()) {
			case OLDEST -> new ArrayDeque<>();
			case RANDOM -> new ArrayList<>();
		};
	}
}
