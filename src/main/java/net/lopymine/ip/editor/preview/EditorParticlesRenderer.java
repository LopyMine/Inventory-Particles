package net.lopymine.ip.editor.preview;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.*;
import net.lopymine.ip.element.mod.spawner.ParticleSpawner;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.lopymine.ip.element.texture.provider.ITextureProvider;
import net.lopymine.ip.renderer.*;
import net.lopymine.ip.utils.*;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EditorParticlesRenderer extends AbstractInventoryElementsRenderer<InventoryParticle> {

	@Getter
	private final InventoryCursor cursor = new InventoryCursor();
	private final List<ParticleSpawner> spawners = new ArrayList<>();

	@Nullable
	private ParticleConfig config;
	private Collection<ParticleHolder> holders = List.of();

	private static final int MAX_TICKS_PER_FRAME = 20;

	@Setter
	private double ticksPerSecond = 20.0D;

	private double tickAccumulator;
	private float tickProgress;

	public EditorParticlesRenderer() {
		this.init();
	}

	public void setSource(@Nullable ParticleConfig config, Collection<ParticleHolder> holders) {
		this.config = config;
		this.holders = holders;
		this.rebuild();
	}

	public void rebuild() {
		this.getScreenElements().clear();
		this.getPendingElements().clear();
		this.spawners.clear();

		if (this.config == null) {
			return;
		}

		ITextureProvider.CACHED_SPRITES.remove(this.config);

		for (ParticleHolder holder : this.holders) {
			this.spawners.add(holder.createSpawner(this.config::createParticle));
		}
	}

	public void render(GuiGraphicsExtractor context) {
		this.render(context, this.tickProgress);
	}

	public void advance(double x, double y, ItemStack stack) {
		double frameTicks = TickUtils.getRealtimeDeltaTicks();
		this.tickAccumulator += frameTicks * (this.ticksPerSecond / 20.0D);

		int ticks = (int) this.tickAccumulator;
		if (ticks > MAX_TICKS_PER_FRAME) {
			ticks = MAX_TICKS_PER_FRAME;
			this.tickAccumulator = ticks;
		}

		for (int i = 0; i < ticks; i++) {
			this.tick(x, y, stack);
		}

		this.tickAccumulator -= ticks;
		this.tickProgress = (float) this.tickAccumulator;
	}

	public void spawn(double x, double y, ItemStack stack, boolean single) {
		if (this.config == null || this.spawners.isEmpty() || stack.isEmpty()) {
			return;
		}

		this.runSoft(() -> {
			ParticleSpawnContext context = ParticleSpawnContext.cursor(this.cursor);
			context.setStack(stack);
			context.setX(x);
			context.setY(y);
			context.setImpulseX(0.0D);
			context.setImpulseY(0.0D);

			List<InventoryParticle> particles = new ArrayList<>();
			for (ParticleSpawner spawner : this.spawners) {
				particles.addAll(single ? spawner.spawnOne(context) : spawner.spawn(context));
			}
			particles.forEach(this::addPendingElement);
			this.pushPendingElements();
		}, "spawning_editor_preview_particles");
	}

	public void tick(double x, double y, ItemStack stack) {
		this.cursor.setStack(stack);
		this.cursor.setMouseX(x);
		this.cursor.setMouseY(y);

		if (this.spawners.isEmpty() || stack.isEmpty() || !this.shouldTick()) {
			return;
		}

		this.runSoft(() -> {
			this.cursor.tick();

			List<InventoryParticle> particles = new ArrayList<>();
			for (ParticleSpawner spawner : this.spawners) {
				particles.addAll(spawner.tickAndSpawn(ParticleSpawnContext.cursor(this.cursor)));
				particles.addAll(spawner.spawnFromCursor(this.cursor));
			}
			particles.forEach(this::addPendingElement);

			this.pushPendingElements();
			this.tickElements();
		}, "ticking_editor_preview_particles");
	}

	@Override
	protected void renderElements(GuiGraphicsExtractor context, float tickProgress) {
		for (InventoryParticle particle : this.getScreenElements()) {
			if (particle == null) {
				continue;
			}
			particle.render(context, this.cursor, tickProgress, this.isStoppedTicking());
		}
		OptimizedDrawer.endDrawing();
	}

	@Override
	protected void runSoft(Runnable runnable, String action) {
		try {
			runnable.run();
		} catch (Exception e) {
			this.getLogger().error("[{}] Unexpected error!", action, e);
			this.getScreenElements().clear();
			this.getPendingElements().clear();
		}
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryParticles.LOGGER;
	}

	@Override
	protected String getModName() {
		return InventoryParticles.MOD_NAME;
	}
}
