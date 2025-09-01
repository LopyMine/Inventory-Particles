package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.spawn.ParticleSpawnType;
import net.lopymine.ip.element.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.resourcepack.ResourcePackParticleConfigsManager;
import net.lopymine.ip.spawner.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class InventoryParticlesRenderer extends TickElement {

	private static final InventoryParticlesRenderer INSTANCE = new InventoryParticlesRenderer();

	private final List<InventoryParticle> renderingParticles = new ArrayList<>();
	@Getter
	private final LinkedList<InventoryParticle> screenParticles = new LinkedList<>();
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
		this.renderingParticles.addAll(this.screenParticles);
		for (InventoryParticle screenParticle : this.renderingParticles) {
			if (screenParticle == null) {
				continue;
			}
			screenParticle.render(context, this.cursor, tickProgress);
			if (screenParticle.isHovered()) {
				this.hoveredParticle = screenParticle;
			}
		}
		this.renderingParticles.clear();
	}

	public void updateCursor(int mouseY, int mouseX, ItemStack item) {
		this.cursor.setMouseY(mouseY);
		this.cursor.setMouseX(mouseX);
		this.cursor.setCurrentStack(item);
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

		this.cursor.tick();

		if (InventoryParticlesConfig.getInstance().getParticleSpawnType() == ParticleSpawnType.EVERYWHERE) {
			for (Slot slot : handler.slots) {
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

		this.screenParticles.removeIf((particle) -> {
			if (particle == null) {
				return true;
			}
			particle.tick();
			return particle.isDead() && !particle.isSelected();
		});
	}

	private void spawnParticle(InventoryParticle particle) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		int difference = (this.screenParticles.size() + 1) - config.getMaxParticles();
		if (difference > 0) {
			this.clearParticlesForNewOnes(difference, config.getParticleDeletionMode());
		}
		this.screenParticles.add(particle);
	}

	private void clearParticlesForNewOnes(int difference, ParticleDeletionMode mode) {
		switch (mode) {
			case OLDEST -> {
				for (int i = 0; i < difference; i++) {
					this.screenParticles.pollFirst();
				}
			}
			case RANDOM -> {
				for (int i = 0; i < difference; i++) {
					this.screenParticles.remove(this.random.nextBetween(0, this.screenParticles.size() - 1));
				}
			}
		}
	}

	public void clear() {
		this.screenParticles.clear();
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
	}
}
