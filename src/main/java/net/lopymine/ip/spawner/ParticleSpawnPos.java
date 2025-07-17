package net.lopymine.ip.spawner;

public record ParticleSpawnPos(int x, int y, int width, int height) implements IParticleSpawnPos {

	@Override
	public int getXOffset() {
		return (this.width - 16) / 2;
	}

	@Override
	public int getYOffset() {
		return (this.height - 16) / 2;
	}
}
