package net.lopymine.ip.utils;

import java.util.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.OptionalExtension;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;

//? if >=1.21 {
/*import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
*///?} else {
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.nbt.*;
//?}

@ExtensionMethod(OptionalExtension.class)
public class NbtUtils {

	@SuppressWarnings("all")
	public static final Optional<Integer[]> NO_EFFECTS_COLOR = Optional.of(new Integer[]{-13083194});

	public static Optional<Integer[]> getColorsFromStack(ItemStack stack) {
		if (stack.isOf(Items.POTION) ||
				stack.isOf(Items.LINGERING_POTION) ||
				stack.isOf(Items.SPLASH_POTION) ||
				stack.isOf(Items.TIPPED_ARROW)
		) {
			return getColorFromPotionContentsStack(stack);
		}

		if (stack.isOf(Items.CROSSBOW)) {
			//? if >=1.21 {
			/*return Optional.ofNullable(stack.getComponents().get(DataComponentTypes.CHARGED_PROJECTILES))
					.map(ChargedProjectilesComponent::getProjectiles)
					.filter((list) -> !list.isEmpty())
					.map((list) -> getColorFromPotionContentsStack(list.get(0)))
					.filter(Optional::isPresent)
					.map(Optional::get);
			*///?} else {
			return Optional.ofNullable(stack.getNbt())
					.to("ChargedProjectiles", NbtList.class)
					.toEmpty(false)
					.toFirst(NbtCompound.class)
					.to("tag", NbtCompound.class)
					.map(NbtUtils::getColorFromPotionNbt)
					.filter(Optional::isPresent)
					.map(Optional::get);
			//?}
		}

		if (/*? if >=1.21 {*/ /*stack.isIn(net.minecraft.registry.tag.ItemTags.DYEABLE) *//*?} else {*/ stack.getItem() instanceof DyeableItem /*?}*/ ) {
			return getColorFromDyedStack(stack);
		}

		if (stack.isOf(Items.FIREWORK_STAR)) {
			return getColorFromFireworkExplosionStack(stack);
		}

		if (stack.isOf(Items.FIREWORK_ROCKET)) {
			return getColorFromFirework(stack);
		}

		return Optional.empty();
	}

	public static Optional<Integer[]> getColorFromFirework(ItemStack stack) {
		//? if >=1.21 {
		/*return Optional.ofNullable(stack.getComponents().get(DataComponentTypes.FIREWORKS))
				.map(FireworksComponent::explosions)
				.map((c) -> c.stream()
						.map(FireworkExplosionComponent::colors)
						.flatMap((o) -> o.intStream().boxed())
						.toArray(Integer[]::new)
				);
		*///?} else {
		return Optional.ofNullable(stack.getNbt())
				.to("Fireworks", NbtCompound.class)
				.to("Explosions", NbtList.class)
				.toEmpty(false)
				.map((l) -> l.stream()
						.map(NbtUtils::getColorFromFireworkExplosionStack)
						.flatMap((o) -> o.stream().flatMap(Arrays::stream))
						.toArray(Integer[]::new)
				);

		//?}
	}

	public static Optional<Integer[]> getColorFromFireworkExplosionStack(ItemStack stack) {
		//? if >=1.21 {
		/*return Optional.ofNullable(stack.getComponents().get(DataComponentTypes.FIREWORK_EXPLOSION))
				.map(NbtUtils::getColorFromFireworkExplosionStack);
		*///?} else {
		return Optional.ofNullable(stack.getNbt())
				.to("Explosion")
				.map(NbtUtils::getColorFromFireworkExplosionStack)
				.filter(Optional::isPresent)
				.map(Optional::get);
		//?}
	}

	//? if >=1.21 {
	/*private static Integer[] getColorFromFireworkExplosionStack(FireworkExplosionComponent component) {
		Integer[] colors = new Integer[component.colors().size()];

		for (int i = 0; i < component.colors().size(); i++) {
			colors[i] = notZeroAlpha(component.colors().getInt(i));
		}

		return colors;
	}
	*///?} else {
	public static Optional<Integer[]> getColorFromFireworkExplosionStack(NbtElement element) {
		return Optional.ofNullable(element)
				.to(NbtCompound.class)
				.to("Colors", NbtIntArray.class)
				.toEmpty(false)
				.map((l) -> l.stream()
						.map((NbtInt::intValue))
						.map(NbtUtils::notZeroAlpha)
						.toArray(Integer[]::new));
	}
	//?}

	public static Optional<Integer[]> getColorFromPotionContentsStack(ItemStack stack) {
		//? if >=1.21 {
		/*PotionContentsComponent component = stack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
		if (component != null) {
			Optional<Integer> optional = component.customColor();
			if (optional.isPresent()) {
				return Optional.of(new Integer[]{optional.get()});
			}
		}
		if (component == null) {
			return Optional.empty();
		}
		Iterable<StatusEffectInstance> effects = component.getEffects();
		List<Integer> colors = new ArrayList<>();
		effects.forEach((effect) -> {
			colors.add(notZeroAlpha(effect.getEffectType().value().getColor()));
		});
		if (colors.isEmpty()) {
			return Optional.of(new Integer[]{-13083194});
		}
		return Optional.of(colors.toArray(Integer[]::new));
		*///?} else {
		return Optional.ofNullable(stack.getNbt())
				.map(NbtUtils::getColorFromPotionNbt)
				.filter(Optional::isPresent)
				.map(Optional::get);
		//?}
	}

	public static Optional<Integer[]> getColorFromDyedStack(ItemStack stack) {
		//? if >=1.21 {
		/*return Optional.ofNullable(stack.getComponents().get(DataComponentTypes.DYED_COLOR))
				.map(DyedColorComponent::rgb)
				.map(NbtUtils::notZeroAlpha)
				.map((i) -> new Integer[]{i});
		*///?} else {
		return Optional.ofNullable(stack.getNbt())
				.to("display", NbtCompound.class)
				.to("color", NbtInt.class)
				.map(NbtInt::intValue)
				.map(NbtUtils::notZeroAlpha)
				.map((i) -> new Integer[]{i});
		//?}
	}

	//? if <=1.20.1 {
	public static Optional<Integer[]> getColorFromPotionNbt(NbtCompound compound) {
		Optional<List<StatusEffectInstance>> optional = Optional.ofNullable(compound.get("Potion"))
				.to(NbtString.class)
				.map(NbtElement::asString)
				.map(Identifier::new)
				.map(Registries.POTION::get)
				.map(Potion::getEffects);
		return optional.isPresent() && optional.get().isEmpty() ? NO_EFFECTS_COLOR : optional
				.map(EffectUtils::mixColors)
				.map((o) -> o.map(NbtUtils::notZeroAlpha))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map((i) -> new Integer[]{i});
	}
	//?}

	private static int notZeroAlpha(int color) {
		int alpha = ArgbUtils2.getAlpha(color);
		if (alpha == 0) {
			return ArgbUtils2.fullAlpha(color);
		}
		return color;
	}

}
