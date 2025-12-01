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
import net.lopymine.ip.utils.ParticleDrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class InventoryParticlesRenderer extends TickElement {

	private static final InventoryParticlesRenderer INSTANCE = new InventoryParticlesRenderer();

	@Getter
	private final Collection<IParticle> screenParticles = getScreenParticlesList();
	private final List<IParticle> pendingParticles = new ArrayList<>();
	private final Random random = Random.create();

	private InventoryCursor cursor = new InventoryCursor();
	private boolean stoppedByInitializationReason;
	private boolean stoppedTicking;
	private int ticksPerTick = 1;
	private int nextTick = 1;
	@Nullable
	private IParticle hoveredParticle;
	@Nullable
	private IParticle selectedParticle;

	private InventoryParticlesRenderer() { }

	public static InventoryParticlesRenderer getInstance() {
		return INSTANCE;
	}

	public void render(DrawContext context, float tickProgress) {
		this.hoveredParticle = null;
		if (this.screenParticles.isEmpty()) {
			return;
		}
		this.runSoft(() -> {
			ParticleDrawUtils.prepareParticlesBuffer();
			for (IParticle particle : this.screenParticles) {
				if (particle == null) {
					continue;
				}
				particle.render(context, this.cursor, tickProgress, this.isStoppedTicking());
				if (particle.isHovered()) {
					this.hoveredParticle = particle;
				}
			}
			ParticleDrawUtils.endParticlesBuffer();
		}, "rendering_particle");
	}

	public void updateCursor(int mouseY, int mouseX, ItemStack item, @Nullable Slot focusedSlot) {
		this.cursor.setMouseY(mouseY);
		this.cursor.setMouseX(mouseX);
		this.cursor.setCurrentStack(item);
		this.cursor.setHoveredSlot(focusedSlot);
	}

	public void tick(@Nullable ScreenHandler handler, @Nullable Integer inventoryX, @Nullable Integer inventoryY) {
		if (this.stoppedTicking || this.stoppedByInitializationReason) {
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
			if (config.isGuiSlotsSpawnEnabled() && inventoryX != null && inventoryY != null && handler != null) {
				this.spawnAllSlotsParticles(handler, inventoryX, inventoryY);
			}
			if (config.isHoveredSlotSpawnEnabled() && inventoryX != null && inventoryY != null) {
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
			ParticleSpawnContext context = ParticleSpawnContext.slots(slot, inventoryX, inventoryY);
			for (IParticleSpawner spawner : particleSpawners) {
				for (InventoryParticle particle : spawner.tickAndSpawn(context)) {
					this.spawnParticle(particle);
				}
			}
		}
	}

	private void spawnCursorParticles() {
		ItemStack stack = this.cursor.getCurrentStack();
		if (stack.isEmpty()) {
			return;
		}
		List<IParticleSpawner> particleSpawners = ResourcePackParticleConfigsManager.getPerItemParticleSpawners().get(stack.getItem());
		if (particleSpawners != null) {
			List<InventoryParticle> particles = new ArrayList<>();

			for (IParticleSpawner spawner : particleSpawners) {
				particles.addAll(spawner.tickAndSpawn(ParticleSpawnContext.cursor(this.cursor)));
				particles.addAll(spawner.spawnFromCursor(this.cursor));
			}

			particles.forEach(this::spawnParticle);
		}
	}

	public void spawnParticle(IParticle particle) {
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
				if (!(this.screenParticles instanceof ArrayDeque<IParticle> deque)) {
					return;
				}
				for (int i = 0; i < difference; i++) {
					deque.pollFirst();
				}
			}
			case RANDOM -> {
				if (!(this.screenParticles instanceof ArrayList<IParticle> list)) {
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

		if (!this.isStoppedTicking()) {
			return;
		}

		if (this.selectedParticle != null) {
			this.selectedParticle.setSelected(false);
			this.selectedParticle = null;
			this.playClickSound();
			return;
		}

		if (this.hoveredParticle == null) {
			return;
		}

		this.hoveredParticle.setSelected(true);
		this.selectedParticle = this.hoveredParticle;
		this.playClickSound();
	}

	private void playClickSound() {
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	public void updateParticlesPositions(double xCoefficient, double yCoefficient) {
		for (IParticle particle : this.screenParticles) {
			particle.setX(particle.getX() * xCoefficient);
			particle.setY(particle.getY() * yCoefficient);
		}
		for (IParticle particle : this.pendingParticles) {
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
				MutableText text = Text.literal("[%s] ".formatted(InventoryParticles.MOD_NAME)).append(Text.literal("Unexpected error with id \"%s\", please report this issue with your game logs! Mod was automatically disabled to prevent spamming ^^".formatted(action)).formatted(Formatting.RED));
				player.sendMessage(text, false);
			}
			InventoryParticlesClient.LOGGER.error("[{}] Failed to process inventory particles!", action, e);
			InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
			config.getMainConfig().setModEnabled(false);
			config.saveAsync();
		}
	}

	public void onPutInSlot(Slot slot, ItemStack stack, int inventoryX, int inventoryY) {
		if (stack.isEmpty() && !slot.hasStack()){
			return;
		}
		int chanceOfSpawn = 100 - (int) Math.ceil(InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGuiActionConfig().getCooldownCoefficient());
		int r = this.random.nextBetween(0, 100);
		if (r < chanceOfSpawn) {
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

	private static @NotNull Collection<IParticle> getScreenParticlesList() {
		return switch (InventoryParticlesConfig.getInstance().getParticleConfig().getParticleDeletionMode()) {
			case OLDEST -> new ArrayDeque<>();
			case RANDOM -> new ArrayList<>();
		};
	}
}
