package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticlesDeletionMode;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.config.sub.InventoryParticlesItemWhitelistsConfig.ParticlesItemWhitelistConfig;
import net.lopymine.ip.element.*;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.particles.ParticlesConfigsManager;
import net.lopymine.ip.spawner.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.lopymine.ip.utils.ParticleDrawUtils;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class InventoryParticlesRenderer extends TickElement {

	private static final InventoryParticlesRenderer INSTANCE = new InventoryParticlesRenderer();

	@Getter
	private Collection<IParticle> screenParticles = getScreenParticlesList();
	private final List<IParticle> pendingParticles = new ArrayList<>();
	private final RandomSource random = RandomSource.create();

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

	public void render(GuiGraphics context, float tickProgress) {
		this.hoveredParticle = null;
		Collection<IParticle> screenParticles = this.screenParticles;
		if (screenParticles.isEmpty()) {
			return;
		}
		this.runSoft(() -> {

			for (IParticle particle : screenParticles) {
				if (particle == null) {
					continue;
				}
				particle.render(context, this.cursor, tickProgress, this.isStoppedTicking());
				if (particle.isHovered()) {
					this.hoveredParticle = particle;
				}
			}
			ParticleDrawUtils.endDrawing();
		}, "rendering_particle");
	}

	public void tick(@Nullable AbstractContainerMenu handler, @Nullable Integer inventoryX, @Nullable Integer inventoryY) {
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
			if (config.isGuiActionsSpawnEnabled()) {
				if (config.isGuiSlotsSpawnEnabled() && inventoryX != null && inventoryY != null && handler != null) {
					this.spawnGuiSlotsParticles(handler, inventoryX, inventoryY);
				}
				if (config.isHoveredSlotSpawnEnabled() && inventoryX != null && inventoryY != null) {
					this.spawnHoveredSlotParticles(inventoryX, inventoryY);
				}
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
		ItemStack stack = hoveredSlot.getItem();
		if (stack.isEmpty()) {
			return;
		}
		Item item = stack.getItem();
		ParticlesItemWhitelistConfig config = InventoryParticlesConfig.getInstance().getWhitelistsConfig().getHoveredSlotConfig();
		if (config.cannotProcess(item)) {
			return;
		}
		List<IParticleSpawner> spawners = ParticlesConfigsManager.getSpawnersForItem(item);
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

	private void spawnGuiSlotsParticles(AbstractContainerMenu handler, int inventoryX, int inventoryY) {
		for (Slot slot : handler.slots) {
			if (this.cursor.getHoveredSlot() != null && this.cursor.getHoveredSlot().index == slot.index) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (stack.isEmpty()) {
				continue;
			}

			Item item = stack.getItem();
			ParticlesItemWhitelistConfig config = InventoryParticlesConfig.getInstance().getWhitelistsConfig().getGuiSlotsConfig();
			if (config.cannotProcess(item)) {
				continue;
			}

			List<IParticleSpawner> particleSpawners = ParticlesConfigsManager.getSpawnersForItem(item);
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
		Item item = stack.getItem();
		ParticlesItemWhitelistConfig config = InventoryParticlesConfig.getInstance().getWhitelistsConfig().getCursorConfig();
		if (config.cannotProcess(item)) {
			return;
		}
		List<IParticleSpawner> particleSpawners = ParticlesConfigsManager.getSpawnersForItem(item);
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
		int difference = (this.screenParticles.size() + 1) - config.getParticleConfig().getParticlesCountLimit();
		if (difference > 0) {
			this.clearParticlesForNewOnes(difference, config.getParticleConfig().getParticlesDeletionMode());
		}
		this.pendingParticles.add(particle);
	}

	private void clearParticlesForNewOnes(int difference, ParticlesDeletionMode mode) {
		int fadeOutDurationTicks = InventoryParticlesConfig.getInstance().getParticleConfig().getFadeOutDurationTicks();

		switch (mode) {
			case OLDEST -> {
				if (!(this.screenParticles instanceof ArrayDeque<IParticle> deque)) {
					return;
				}
				if (deque.isEmpty()) {
					return;
				}
				ArrayDeque<IParticle> particles = new ArrayDeque<>(deque);
				Iterator<IParticle> iterator = particles.iterator();
				for (int i = 0; i < difference && iterator.hasNext(); i++) {
					IParticle particle = iterator.next();
					if (fadeOutDurationTicks == 0) {
						iterator.remove();
					} else {
						if (particle instanceof InventoryParticle inventoryParticle && !inventoryParticle.isFadingOut()) {
							inventoryParticle.startFadeOut();
						}
					}
				}
				if (fadeOutDurationTicks == 0) {
					this.screenParticles = particles;
				}
			}
			case RANDOM -> {
				if (!(this.screenParticles instanceof ArrayList<IParticle> list)) {
					return;
				}
				if (list.isEmpty()) {
					return;
				}
				ArrayList<IParticle> particles = new ArrayList<>(list);
				for (int i = 0; i < difference; i++) {
					if (particles.isEmpty()) {
						break;
					}
					int index = this.random.nextIntBetweenInclusive(0, particles.size() - 1);
					if (fadeOutDurationTicks == 0) {
						particles.remove(index);
					} else {
						IParticle particle = particles.get(index);
						if (particle instanceof InventoryParticle inventoryParticle && !inventoryParticle.isFadingOut()) {
							inventoryParticle.startFadeOut();
						}
					}
				}
				if (fadeOutDurationTicks == 0) {
					this.screenParticles = particles;
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
		double mouseX = Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth();
		double mouseY = Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight();

		this.cursor.setMouseX(mouseX);
		this.cursor.setX(mouseX);
		this.cursor.setLastX(mouseX);

		this.cursor.setMouseY(mouseY);
		this.cursor.setY(mouseY);
		this.cursor.setLastY(mouseY);

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
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				MutableComponent text = Component.literal("[%s] ".formatted(InventoryParticles.MOD_NAME)).append(Component.literal("Unexpected error with id \"%s\", please report this issue with your game logs! Mod was automatically disabled to prevent spamming ^^".formatted(action)).withStyle(ChatFormatting.RED));
				player.displayClientMessage(text, false);
			}
			InventoryParticlesClient.LOGGER.error("[{}] Failed to process inventory particles!", action, e);
			InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
			config.getMainConfig().setModEnabled(false);
			config.saveAsync();
		}
	}

	public void onGuiAction(Slot slot, ItemStack stack, int inventoryX, int inventoryY) {
		if (stack.isEmpty() && !slot.hasItem()){
			return;
		}
		Item item = stack.isEmpty() ? slot.getItem().getItem() : stack.getItem();
		ParticlesItemWhitelistConfig config = InventoryParticlesConfig.getInstance().getWhitelistsConfig().getGuiSlotsConfig();
		if (config.cannotProcess(item)) {
			return;
		}
		int chanceOfSpawn = 100 - (int) Math.ceil(InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGuiActionConfig().getCooldownCoefficient());
		int r = this.random.nextIntBetweenInclusive(0, 100);
		if (r < chanceOfSpawn) {
			return;
		}
		this.runSoft(() -> {
			List<IParticleSpawner> spawners = ParticlesConfigsManager.getSpawnersForItem(item);
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
		return switch (InventoryParticlesConfig.getInstance().getParticleConfig().getParticlesDeletionMode()) {
			case OLDEST -> new ArrayDeque<>();
			case RANDOM -> new ArrayList<>();
		};
	}
}
