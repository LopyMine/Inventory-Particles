package net.lopymine.ip.config.color;

import java.util.*;
import lombok.*;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class NbtListParticleColorType implements IParticleColorType, IListParticleColorType {

	public static final Integer[] NO_COLOR = {-1};
	private Integer[] colors;
	@Nullable
	private Integer currentColor;

	private static Integer[] getColorFromStack(ItemStack stack) {
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

		return NO_COLOR;
	}

	private static Integer[] getColorFromFireworkExplosionStack(ItemStack stack) {
		FireworkExplosionComponent component = stack.getComponents().get(DataComponentTypes.FIREWORK_EXPLOSION);
		if (component == null) {
			return NO_COLOR;
		}

		Integer[] colors = new Integer[component.colors().size()];

		for (int i = 0; i < component.colors().size(); i++) {
			colors[i] = notZeroAlpha(component.colors().getInt(i));
		}

		return colors;
	}

	private static Integer[] getColorFromPotionContentsStack(ItemStack stack) {
		PotionContentsComponent component = stack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
		if (component == null) {
			return NO_COLOR;
		}

		Iterable<StatusEffectInstance> effects = component.getEffects();
		List<Integer> colors = new ArrayList<>();
		effects.forEach((effect) -> {
			colors.add(notZeroAlpha(effect.getEffectType().value().getColor()));
		});
		return colors.toArray(Integer[]::new);
	}

	private static Integer[] getColorFromDyedStack(ItemStack stack) {
		DyedColorComponent component = stack.getComponents().get(DataComponentTypes.DYED_COLOR);
		if (component == null) {
			return NO_COLOR;
		}
		return new Integer[]{notZeroAlpha(component.rgb())};
	}

	private static int notZeroAlpha(int color) {
		int alpha = ArgbUtils.getAlpha(color);
		if (alpha == 0) {
			return ArgbUtils.fullAlpha(color);
		}
		return color;
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
