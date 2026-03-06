package net.lopymine.ip.element.color;

import net.minecraft.util.RandomSource;

public class StandardColorProvider implements IColorProvider {

	@Override
	public int tick(RandomSource random) {
		return -1;
	}

	@Override
	public IColorProvider copy() {
		return new StandardColorProvider();
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
