package net.lopymine.ip.config.color;

import java.util.*;
import lombok.*;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
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
		if (stack.isOf(Items.POTION) ||
				stack.isOf(Items.LINGERING_POTION) ||
				stack.isOf(Items.SPLASH_POTION) ||
				stack.isOf(Items.TIPPED_ARROW)
		) {
			return getColorFromPotionContentsStack(stack);
		}

		if (stack.isOf(Items.CROSSBOW)) {
			ChargedProjectilesComponent component = stack.getComponents().get(DataComponentTypes.CHARGED_PROJECTILES);
			if (component == null) {
				return NO_COLOR;
			}
			List<ItemStack> projectiles = component.getProjectiles();
			if (projectiles.isEmpty()) {
				return NO_COLOR;
			}
			ItemStack itemStack = projectiles.get(0);
			return getColorFromPotionContentsStack(itemStack);
		}

		if (stack.isIn(ItemTags.DYEABLE)) {
			return getColorFromDyedStack(stack);
		}

		if (stack.isOf(Items.FIREWORK_STAR)) {
			return getColorFromFireworkExplosionStack(stack);
		}

		if (stack.isOf(Items.FIREWORK_ROCKET)) {
			return getColorFromFirework(stack);
		}

		return NO_COLOR;
	}

	private static int getColorFromFirework(ItemStack stack) {
		FireworksComponent component = stack.getComponents().get(DataComponentTypes.FIREWORKS);
		if (component == null) {
			return NO_COLOR;
		}

		Integer[] colors = new Integer[component.explosions().size()];

		for (int i = 0; i < component.explosions().size(); i++) {
			colors[i] = ArgbUtils.mix(getColorFromFireworkExplosionStack(component.explosions().get(i)));
		}

		return ArgbUtils.mix(colors);
	}

	private static int getColorFromFireworkExplosionStack(ItemStack stack) {
		FireworkExplosionComponent component = stack.getComponents().get(DataComponentTypes.FIREWORK_EXPLOSION);
		if (component == null) {
			return NO_COLOR;
		}
		return ArgbUtils.mix(getColorFromFireworkExplosionStack(component));
	}

	private static Integer[] getColorFromFireworkExplosionStack(FireworkExplosionComponent component) {
		Integer[] colors = new Integer[component.colors().size()];

		for (int i = 0; i < component.colors().size(); i++) {
			colors[i] = notZeroAlpha(component.colors().getInt(i));
		}

		return colors;
	}

	private static int getColorFromPotionContentsStack(ItemStack stack) {
		PotionContentsComponent component = stack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
		if (component == null) {
			return NO_COLOR;
		}
		return notZeroAlpha(component.getColor());
	}

	private static int getColorFromDyedStack(ItemStack stack) {
		DyedColorComponent component = stack.getComponents().get(DataComponentTypes.DYED_COLOR);
		if (component == null) {
			return NO_COLOR;
		}
		return notZeroAlpha(component.rgb());
	}

	private static int notZeroAlpha(int color) {
		int alpha = ArgbUtils.getAlpha(color);
		if (alpha == 0) {
			return ArgbUtils.fullAlpha(color);
		}
		return color;
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
