package net.lopymine.ip.element.color;

import lombok.*;
import net.lopymine.ip.utils.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

@Getter
@Setter
public class NbtColorProvider implements IColorProvider {

	public static final int NO_COLOR = -1;
	private int color;

	@Override
	public int tick(RandomSource random) {
		return this.color;
	}

	@Override
	public IColorProvider copy() {
		return new NbtColorProvider();
	}

	@Override
	public void compile(ItemStack stack, RandomSource random) {
		this.color = getColorFromStack(stack);
	}

	private static int getColorFromStack(ItemStack stack) {
		return NbtUtils.getColorsFromStack(stack).map(ArgbUtils2::mix).orElse(NO_COLOR);
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
