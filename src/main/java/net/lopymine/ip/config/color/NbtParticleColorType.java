package net.lopymine.ip.config.color;

import java.util.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;

public class NbtParticleColorType implements IParticleColorType {

	@Override
	public int getColor(ItemStack stack, Random random) {
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
				return -1;
			}
			List<ItemStack> projectiles = component.getProjectiles();
			if (projectiles.isEmpty()) {
				return -1;
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

		return -1;
	}

	private static int getColorFromFireworkExplosionStack(ItemStack stack) {
		FireworkExplosionComponent component = stack.getComponents().get(DataComponentTypes.FIREWORK_EXPLOSION);
		if (component == null) {
			return -1;
		}

		int red = 0;
		int green = 0;
		int blue = 0;
		int count = 0;

		for (Integer color : component.colors()) {
			red += ColorHelper.getRed(color);
			green += ColorHelper.getGreen(color);
			blue += ColorHelper.getBlue(color);
			count++;
		}

		return ColorHelper.getArgb(red / count, green / count, blue / count);
	}
	private static int getColorFromPotionContentsStack(ItemStack stack) {
		PotionContentsComponent component = stack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
		if (component == null) {
			return -1;
		}
		return component.getColor(-1);
	}

	private static int getColorFromDyedStack(ItemStack stack) {
		DyedColorComponent component = stack.getComponents().get(DataComponentTypes.DYED_COLOR);
		if (component == null) {
			return -1;
		}
		return ColorHelper.fullAlpha(component.rgb());
	}

	@Override
	public String getAsString() {
		return "nbt";
	}
}
