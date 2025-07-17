package net.lopymine.ip.config.color;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class StandardParticleColorType implements IParticleColorType {

	@Override
	public int getColor(ItemStack stack, Random random) {
		return -1;
	}

	@Override
	public String getAsString() {
		return "";
	}
}
