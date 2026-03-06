package net.lopymine.ip.element.color;

import lombok.*;
import net.lopymine.ip.utils.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class NbtListColorProvider implements IColorProvider, IListInventoryElementColorProvider {

	public static final Integer[] NO_COLOR = {-1};
	private Integer[] colors;
	@Nullable
	private Integer currentColor;

	private static Integer[] getColorFromStack(ItemStack stack) {
		return NbtUtils.getColorsFromStack(stack).orElse(NO_COLOR);
	}

	@Override
	public Integer[] getList() {
		return this.colors;
	}

	@Override
	public int tick(RandomSource random) {
		if (this.currentColor != null) {
			return this.currentColor;
		}
		if (this.colors.length == 0) {
			return -1;
		}
		return this.currentColor = this.colors[random.nextIntBetweenInclusive(0, this.colors.length - 1)];
	}

	@Override
	public IColorProvider copy() {
		return new NbtListColorProvider();
	}

	@Override
	public void compile(ItemStack stack, RandomSource random) {
		this.colors = getColorFromStack(stack);
	}

	@Override
	public String asString() {
		return "nbt_list";
	}

	@Override
	public String toString() {
		return this.asString();
	}
}
