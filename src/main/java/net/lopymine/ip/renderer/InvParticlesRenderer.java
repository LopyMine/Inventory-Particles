package net.lopymine.ip.renderer;

import java.util.*;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.element.*;
import net.lopymine.ip.manager.InvParticleConfigManager;
import net.lopymine.ip.spawner.ParticleSpawner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class InvParticlesRenderer {

	private static final InvParticlesRenderer INSTANCE = new InvParticlesRenderer();

	private final List<InvParticle> renderingParticles = new ArrayList<>();
	@Getter
	private final List<InvParticle> screenParticles = new LinkedList<>();
	private final Random random = Random.create();
	@Getter
	private final Cursor cursor = new Cursor();
	@Setter
	@Getter
	private boolean stopTicking;
	@Nullable
	@Setter
	@Getter
	private InvParticle hoveredParticle;
	@Getter
	private boolean lockHoveredParticle;

	private InvParticlesRenderer() {
	}

	public static InvParticlesRenderer getInstance() {
		return INSTANCE;
	}

	public void render(DrawContext context, float tickProgress) {
		if (!this.lockHoveredParticle) {
			this.hoveredParticle = null;
		}
		this.renderingParticles.addAll(this.screenParticles);
		for (InvParticle screenParticle : this.renderingParticles) {
			if (screenParticle == null) {
				continue;
			}
			screenParticle.render(context, this.cursor, tickProgress);
		}
		this.renderingParticles.clear();
	}

	public void updateCursor(int mouseY, int mouseX, Item item) {
		this.cursor.setMouseY(mouseY);
		this.cursor.setMouseX(mouseX);
		this.cursor.setCurrentItem(item);
	}

	public void tick() {
		if (this.stopTicking) {
			return;
		}
		this.cursor.tick();

		Item currentItem = this.cursor.getCurrentItem();
		List<ParticleSpawner> particleSpawners = InvParticleConfigManager.getItemParticleConfigs().get(currentItem);
		if (currentItem != Items.AIR && particleSpawners != null) {
			List<InvParticle> particles = new ArrayList<>();
			for (ParticleSpawner spawner : particleSpawners) {
				particles.addAll(spawner.tickAndSpawn(this.random, this.cursor));
				particles.addAll(spawner.spawnFromSpeed(this.random, this.cursor));
				//particles.addAll(spawner.spawnFromSpeedDelta(this.random, this.cursor));
			}
			particles.forEach(this::spawnParticle);
		}

		this.screenParticles.removeIf((particle) -> {
			if (particle == null) {
				return true;
			}
			particle.tick();
			return particle.isDead();
		});
	}

	private void spawnParticle(InvParticle particle) {
		this.screenParticles.add(particle);
	}

	public void clear() {
		this.screenParticles.clear();
		this.cursor.setCurrentItem(null);
		this.cursor.setMouseX(0);
		this.cursor.setMouseY(0);
		this.cursor.setX(0);
		this.cursor.setY(0);
		this.cursor.setLastX(0);
		this.cursor.setLastY(0);
		this.cursor.setSpeed(0);
		this.cursor.setSpeedX(0);
		this.cursor.setSpeedY(0);
		this.cursor.setLastSpeed(0);
		this.cursor.setLastSpeedX(0);
		this.cursor.setLastSpeedY(0);
	}

	public void lockHoveredParticle() {
		this.lockHoveredParticle = !this.lockHoveredParticle;
		MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
