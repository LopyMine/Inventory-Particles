package net.lopymine.ip.color;

import lombok.*;
import net.lopymine.ip.utils.*;
import net.minecraft.item.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

//? if >=1.21 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
//?}

@Getter
@Setter
public class NbtListParticleColorType implements IParticleColorType, IListParticleColorType {

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
	public int tick(Random random) {
		if (this.currentColor != null) {
			return this.currentColor;
		}
		if (this.colors.length == 0) {
			return -1;
		}
		return this.currentColor = this.colors[random.nextBetween(0, this.colors.length - 1)];
	}

	@Override
	public IParticleColorType copy() {
		return new NbtListParticleColorType();
	}

	@Override
	public void compile(ItemStack stack, Random random) {
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
