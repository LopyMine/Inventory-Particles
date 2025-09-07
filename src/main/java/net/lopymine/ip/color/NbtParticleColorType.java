package net.lopymine.ip.color;

import lombok.*;
import net.lopymine.ip.utils.*;
import net.minecraft.item.*;
import net.minecraft.util.math.random.Random;

@Getter
@Setter
public class NbtParticleColorType implements IParticleColorType {

	public static final int NO_COLOR = -1;
	private int color;

	@Override
	public int tick(Random random) {
		return this.color;
	}

	@Override
	public IParticleColorType copy() {
		return new NbtParticleColorType();
	}

	@Override
	public void compile(ItemStack stack, Random random) {
		this.color = getColorFromStack(stack);
	}

	private static int getColorFromStack(ItemStack stack) {
		return NbtUtils.getColorsFromStack(stack).map(ArgbUtils::mix).orElse(NO_COLOR);
	}

	@Override
	public String asString() {
		return "nbt";
	}

	@Override
	public String toString() {
		return this.getString(this.color);
	}
}
