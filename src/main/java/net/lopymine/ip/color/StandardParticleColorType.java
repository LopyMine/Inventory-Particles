package net.lopymine.ip.color;

import net.minecraft.util.RandomSource;

public class StandardParticleColorType implements IParticleColorType {

	@Override
	public int tick(RandomSource random) {
		return -1;
	}

	@Override
	public IParticleColorType copy() {
		return new StandardParticleColorType();
	}

	@Override
	public String asString() {
		return "standard";
	}

	@Override
	public String toString() {
		return this.getString(-1);
	}

}
