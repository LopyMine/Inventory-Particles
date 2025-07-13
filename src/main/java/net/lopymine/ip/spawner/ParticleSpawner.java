package net.lopymine.ip.spawner;

import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.config.element.*;
import net.lopymine.ip.element.*;
import net.minecraft.util.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ParticleSpawner extends TickElement implements IParticleSpawner {

	@Nullable
	private final ParticleSpawnArea spawnArea;
	private final TwoIntegerRange countRange;
	private final TwoIntegerRange frequencyRange;
	private final float speedCoefficient;
	private final float deltaCoefficient;
	private final Function<Cursor, InvParticle> function;
	private int nextSpawnTicks = 0;

	public ParticleSpawner(Identifier spawnArea, TwoIntegerRange countRange, TwoIntegerRange frequencyRange, float speedCoefficient, float deltaCoefficient, Function<Cursor, InvParticle> function) {
		this.spawnArea        = ParticleSpawnArea.readFromTexture(spawnArea);
		this.countRange       = countRange;
		this.frequencyRange   = frequencyRange;
		this.speedCoefficient = speedCoefficient;
		this.deltaCoefficient = deltaCoefficient;
		this.function         = function;
	}

	public List<InvParticle> spawnFromSpeed(Random random, Cursor cursor) {
		int spawnCount = (int) (random.nextBetween(this.countRange.getMin(), this.countRange.getMax()) * this.speedCoefficient * Math.sqrt(cursor.getSpeed()));
		return this.createParticles(spawnCount, cursor, (particle) -> this.spawnParticleAtCursorDeltaPath(particle, cursor));
	}

	private void spawnParticleAtCursorDeltaPath(InvParticle particle, Cursor cursor) {
		Random random = particle.getRandom();
		int deltaX = cursor.getX() - cursor.getLastX();
		int deltaY = cursor.getY() - cursor.getLastY();
		float progress = random.nextBetween(0, 100) / 100F;
		int pathX = (int) (deltaX * progress);
		int pathY = (int) (deltaY * progress);
		particle.setX(particle.getX() - pathX + random.nextBetween(0, 2));
		particle.setY(particle.getY() - pathY + random.nextBetween(0, 2));
	}

//	public List<InvParticle> spawnFromSpeedDelta(Random random, Cursor cursor) {
//		//double sqrt = Math.sqrt(cursor.getDeltaSpeed());
//		//int spawnCount = (int) (random.nextBetween(this.countRange.getMin(), this.countRange.getMax()) * this.deltaCoefficient * sqrt);
//		//System.out.println(sqrt);
//		//System.out.println(spawnCount + " BBBBB " + cursor.getDeltaSpeed());
//		return List.of();//this.createParticles(spawnCount, cursor);
//	}

	public List<InvParticle> tickAndSpawn(Random random, Cursor cursor) {
		this.tick();

		if (this.nextSpawnTicks == 0) {
			int ticksToWaitForNextSpawn = random.nextBetween(this.frequencyRange.getMin(), this.frequencyRange.getMax());
			this.nextSpawnTicks = this.ticks + ticksToWaitForNextSpawn;
		}

		if (this.ticks < this.nextSpawnTicks) {
			return List.of();
		}

		this.nextSpawnTicks = 0;

		return this.createParticles(random.nextBetween(this.countRange.getMin(), this.countRange.getMax()), cursor);
	}

	private List<InvParticle> createParticles(int spawnCount, Cursor cursor) {
		return createParticles(spawnCount, cursor, (particle) -> {});
	}

	private List<InvParticle> createParticles(int spawnCount, Cursor cursor, Consumer<InvParticle> consumer) {
		List<InvParticle> particles = new ArrayList<>();
		for (int i = 0; i < spawnCount; i++) {
			InvParticle particle = this.function.apply(cursor);
			consumer.accept(particle);

			IParticleSpawnPos particleSpawnPos = this.spawnArea == null ? null : this.spawnArea.getRandomPos(particle.getRandom());
			if (particleSpawnPos != null) {
				particle.setX(particle.getX() - 7.5F + particleSpawnPos.x());
				particle.setY(particle.getY() - 7.5F + particleSpawnPos.y());
			} else {
				particle.setX(particle.getX() + particle.getRandom().nextBetween(-4, 4));
				particle.setY(particle.getY() + particle.getRandom().nextBetween(-4, 4));
			}

			particles.add(particle);
		}
		return particles;
	}

}
