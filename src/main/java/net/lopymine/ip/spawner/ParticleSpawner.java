package net.lopymine.ip.spawner;

import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.color.IParticleColorType;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.controller.color.ColorController;
import net.lopymine.ip.element.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.predicate.IParticleSpawnPredicate;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class ParticleSpawner extends TickElement implements IParticleSpawner {

	private final Random random = Random.create();

	@Nullable
	private ParticleSpawnArea spawnArea;
	private IntegerRange countRange;
	private IntegerRange frequencyRange;
	private double speedCoefficient;
	private IParticleColorType colorType;
	private IParticleSpawnPredicate spawnCondition;
	private Function<ParticleSpawnContext, InventoryParticle> function;
	private int nextSpawnTicks = 0;

	public ParticleSpawner(Identifier spawnArea, IntegerRange countRange, IntegerRange frequencyRange, double speedCoefficient, IParticleColorType colorType, IParticleSpawnPredicate spawnCondition, Function<ParticleSpawnContext, InventoryParticle> function) {
		this.spawnArea        = ParticleSpawnArea.readFromTexture(spawnArea);
		this.countRange       = countRange;
		this.frequencyRange   = frequencyRange;
		this.speedCoefficient = speedCoefficient;
		this.colorType        = colorType;
		this.spawnCondition   = spawnCondition;
		this.function         = function;
	}

	public List<InventoryParticle> spawnFromCursor(InventoryCursor cursor) {
		int spawnCount = (int) (this.random.nextBetween(this.countRange.getMin(), this.countRange.getMax()) * this.speedCoefficient * (Math.sqrt(cursor.getSpeed()) * 1.0F));
		return this.createParticles(spawnCount, cursor, (particle) -> this.spawnParticleAtCursorDeltaPath(particle, cursor));
	}

	private void spawnParticleAtCursorDeltaPath(InventoryParticle particle, InventoryCursor cursor) {
		Random random = particle.getRandom();
		int deltaX = cursor.getX() - cursor.getLastX();
		int deltaY = cursor.getY() - cursor.getLastY();
		float progress = random.nextBetween(0, 100) / 100F;
		int pathX = (int) (deltaX * progress);
		int pathY = (int) (deltaY * progress);
		particle.setX(particle.getX() - pathX + random.nextBetween(0, 2));
		particle.setY(particle.getY() - pathY + random.nextBetween(0, 2));
	}

	public List<InventoryParticle> tickAndSpawn(ParticleSpawnContext context) {
		this.tick();

		if (this.nextSpawnTicks == 0) {
			int ticks = this.random.nextBetween(this.frequencyRange.getMin(), this.frequencyRange.getMax());
			int ticksToWaitForNextSpawn = (int) ((((float) ticks) * context.getCooldownCoefficient()) * InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGlobalConfig().getCooldownCoefficient());
			this.nextSpawnTicks = this.ticks + ticksToWaitForNextSpawn;
		}

		if (this.ticks < this.nextSpawnTicks) {
			return List.of();
		}

		this.nextSpawnTicks = 0;

		return this.spawn(context);
	}

	@Override
	public List<InventoryParticle> spawn(ParticleSpawnContext context) {
		return this.createParticles(this.random.nextBetween(this.countRange.getMin(), this.countRange.getMax()), context, (particle) -> {});
	}

	private List<InventoryParticle> createParticles(int spawnCount, InventoryCursor cursor, Consumer<InventoryParticle> consumer) {
		return this.createParticles(spawnCount, ParticleSpawnContext.cursor(cursor), consumer);
	}

	private List<InventoryParticle> createParticles(int spawnCount, ParticleSpawnContext context, Consumer<InventoryParticle> consumer) {
		if (spawnCount != 0 && !this.spawnCondition.test(context.getStack())) {
			return List.of();
		}

		if (spawnCount == 0 && context == ParticleSpawnContext.GUI_ACTION_SLOT) {
			spawnCount = 1;
		}

		float count = (float) ((((float) spawnCount) * context.getCountCoefficient()) * InventoryParticlesConfig.getInstance().getCoefficientsConfig().getGlobalConfig().getCountCoefficient());
		int countOfParticles = count > 0.0F && count < 1.0F ? 1 : (int) count;

		List<InventoryParticle> particles = new ArrayList<>();
		for (int i = 0; i < countOfParticles; i++) {
			InventoryParticle particle = this.function.apply(context);
			consumer.accept(particle);

			this.offsetParticlePos(particle);
			this.setParticleColorController(particle, context);

			particles.add(particle);
		}

		return particles;
	}

	private void setParticleColorController(InventoryParticle particle, ParticleSpawnContext context) {
		ItemStack currentItem = context.getStack();
		IParticleColorType type = this.colorType.copy();
		type.compile(currentItem, particle.getRandom());
		particle.setColorController(new ColorController<>(type));
	}

	private void offsetParticlePos(InventoryParticle particle) {
		IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());
		particle.setX(particle.getX() - 8D);
		particle.setY(particle.getY() - 8D);
		if (particleSpawnPos != null) {
			particle.setX(particle.getX() - particleSpawnPos.getXOffset() + particleSpawnPos.x() - (particle.getWidth() / 2));
			particle.setY(particle.getY() - particleSpawnPos.getYOffset() + particleSpawnPos.y() - (particle.getHeight() / 2));
		}
		particle.setLastX(particle.getX());
		particle.setLastY(particle.getY());
	}

	@Override
	public void bump(ParticleHolder holder) {
		this.spawnArea        = ParticleSpawnArea.readFromTexture(holder.getSpawnArea());
		this.countRange       = holder.getSpawnCount();
		this.frequencyRange   = holder.getSpawnFrequency();
		this.speedCoefficient = holder.getSpeedCoefficient();
		this.colorType        = holder.getColor();
		this.spawnCondition   = holder.getSpawnCondition();
		this.nextSpawnTicks   = 0;
	}
}
