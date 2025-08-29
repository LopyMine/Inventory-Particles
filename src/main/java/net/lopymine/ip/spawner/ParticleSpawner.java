package net.lopymine.ip.spawner;

import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.config.color.IParticleColorType;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.controller.color.ColorController;
import net.lopymine.ip.element.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.predicate.IParticleSpawnPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ParticleSpawner extends TickElement implements IParticleSpawner {

	private final Random random = Random.create();

	@Nullable
	private final ParticleSpawnArea spawnArea;
	private final IntegerRange countRange;
	private final IntegerRange frequencyRange;
	private final float speedCoefficient;
	private final IParticleColorType colorType;
	private final IParticleSpawnPredicate spawnCondition;
	private final Function<InventoryCursor, InventoryParticle> function;
	private int nextSpawnTicks = 0;

	public ParticleSpawner(Identifier spawnArea, IntegerRange countRange, IntegerRange frequencyRange, float speedCoefficient, IParticleColorType colorType, IParticleSpawnPredicate spawnCondition, Function<InventoryCursor, InventoryParticle> function) {
		this.spawnArea        = ParticleSpawnArea.readFromTexture(spawnArea);
		this.countRange       = countRange;
		this.frequencyRange   = frequencyRange;
		this.speedCoefficient = speedCoefficient;
		this.colorType        = colorType;
		this.spawnCondition   = spawnCondition;
		this.function         = function;
	}

	public List<InventoryParticle> spawnFromSpeed(InventoryCursor cursor) {
		int spawnCount = (int) (this.random.nextBetween(this.countRange.getMin(), this.countRange.getMax()) * this.speedCoefficient * Math.sqrt(cursor.getSpeed()));
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

	public List<InventoryParticle> tickAndSpawn(InventoryCursor cursor) {
		this.tick();

		if (this.nextSpawnTicks == 0) {
			int ticksToWaitForNextSpawn = this.random.nextBetween(this.frequencyRange.getMin(), this.frequencyRange.getMax());
			this.nextSpawnTicks = this.ticks + ticksToWaitForNextSpawn;
		}

		if (this.ticks < this.nextSpawnTicks) {
			return List.of();
		}

		this.nextSpawnTicks = 0;

		return this.createParticles(this.random.nextBetween(this.countRange.getMin(), this.countRange.getMax()), cursor);
	}

	private List<InventoryParticle> createParticles(int spawnCount, InventoryCursor cursor) {
		return createParticles(spawnCount, cursor, (particle) -> {});
	}

	private List<InventoryParticle> createParticles(int spawnCount, InventoryCursor cursor, Consumer<InventoryParticle> consumer) {
		if (spawnCount != 0 && !this.spawnCondition.test(cursor.getCurrentStack())) {
			return List.of();
		}

		List<InventoryParticle> particles = new ArrayList<>();
		for (int i = 0; i < spawnCount; i++) {
			InventoryParticle particle = this.function.apply(cursor);
			consumer.accept(particle);

			this.offsetParticlePos(particle);
			this.setParticleColorController(particle, cursor);

			particles.add(particle);
		}

		return particles;
	}

	private void setParticleColorController(InventoryParticle particle, InventoryCursor cursor) {
		ItemStack currentItem = cursor.getCurrentStack();
		IParticleColorType type = this.colorType.copy();
		type.compile(currentItem, particle.getRandom());
		particle.setColorController(new ColorController<>(type));
	}

	private void offsetParticlePos(InventoryParticle particle) {
		IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());
		particle.setX(particle.getX() - 8F);
		particle.setY(particle.getY() - 8F);
		if (particleSpawnPos != null) {
			particle.setX(particle.getX() - particleSpawnPos.getXOffset() + particleSpawnPos.x() - (particle.getWidth() / 2));
			particle.setY(particle.getY() - particleSpawnPos.getYOffset() + particleSpawnPos.y() - (particle.getHeight() / 2));
		}
		particle.setLastX(particle.getX());
		particle.setLastY(particle.getY());
	}

}
