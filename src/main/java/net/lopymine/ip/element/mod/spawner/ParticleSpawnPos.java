package net.lopymine.ip.element.mod.spawner;

public record ParticleSpawnPos(int x, int y, int width, int height) implements IParticleSpawnPos {

	@Override
	public double getDetailedX() {
		return (int) (((float) this.x / this.width) * 16F);
	}

	@Override
	public double getDetailedY() {
		return (int) (((float) this.y / this.height) * 16F);
	}
}
