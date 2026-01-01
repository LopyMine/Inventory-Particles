package net.lopymine.ip.utils;

import java.util.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.OptionalExtension;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.*;
//?} else {
/*import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;
*///?}

@ExtensionMethod(OptionalExtension.class)
public class NbtUtils {

	@SuppressWarnings("all")
	public static final Optional<Integer[]> NO_EFFECTS_COLOR = Optional.of(new Integer[]{-13083194});

	public static Optional<Integer[]> getColorsFromStack(ItemStack stack) {
		if (stack.is(Items.POTION) ||
				stack.is(Items.LINGERING_POTION) ||
				stack.is(Items.SPLASH_POTION) ||
				stack.is(Items.TIPPED_ARROW)
		) {
			return getColorFromPotionContentsStack(stack);
		}

		if (stack.is(Items.CROSSBOW)) {
			//? if >=1.21 {
			return Optional.ofNullable(stack.getComponents().get(DataComponents.CHARGED_PROJECTILES))
					.map(ChargedProjectiles::getItems)
					.filter((list) -> !list.isEmpty())
					.map((list) -> getColorFromPotionContentsStack(list.get(0)))
					.filter(Optional::isPresent)
					.map(Optional::get);
			//?} else {
			/*return Optional.ofNullable(stack.getTag())
					.to("ChargedProjectiles", ListTag.class)
					.toEmpty(false)
					.toFirst(CompoundTag.class)
					.to("tag", CompoundTag.class)
					.map((tag) -> {
						Optional<Integer[]> a = NbtUtils.getColorFromFirework(tag);
						if (a.isPresent()) {
							return a;
						}
						return NbtUtils.getColorFromPotionNbt(tag);
					})
					.filter(Optional::isPresent)
					.map(Optional::get);
			*///?}
		}

		if (/*? if >=1.21 {*/ stack.is(net.minecraft.tags.ItemTags.DYEABLE) /*?} else {*/ /*stack.getItem() instanceof DyeableLeatherItem *//*?}*/ ) {
			return getColorFromDyedStack(stack);
		}

		if (stack.is(Items.FIREWORK_STAR)) {
			return getColorFromFireworkExplosionStack(stack);
		}

		if (stack.is(Items.FIREWORK_ROCKET)) {
			return getColorFromFirework(stack);
		}

		return Optional.empty();
	}

	//? if >=1.21 {
	public static Optional<Integer[]> getColorFromFirework(ItemStack stack) {
		return Optional.ofNullable(stack.getComponents().get(DataComponents.FIREWORKS))
				.map(Fireworks::explosions)
				.map((c) -> c.stream()
						.map(FireworkExplosion::colors)
						.flatMap((o) -> o.intStream().boxed())
						.toArray(Integer[]::new)
				);
	}
	//?} else {
	/*public static Optional<Integer[]> getColorFromFirework(ItemStack stack) {
		return getColorFromFirework(stack.getTag());
	}

	public static Optional<Integer[]> getColorFromFirework(@Nullable CompoundTag tag) {
		return Optional.ofNullable(tag)
				.to("Fireworks", CompoundTag.class)
				.to("Explosions", ListTag.class)
				.toEmpty(false)
				.map((l) -> l.stream()
						.map(NbtUtils::getColorFromFireworkExplosionStack)
						.flatMap((o) -> o.stream().flatMap(Arrays::stream))
						.toArray(Integer[]::new)
				);
	}
	*///?}

	public static Optional<Integer[]> getColorFromFireworkExplosionStack(ItemStack stack) {
		//? if >=1.21 {
		return Optional.ofNullable(stack.getComponents().get(DataComponents.FIREWORK_EXPLOSION))
				.map(NbtUtils::getColorFromFireworkExplosionStack);
		//?} else {
		/*return Optional.ofNullable(stack.getTag())
				.to("Explosion")
				.map(NbtUtils::getColorFromFireworkExplosionStack)
				.filter(Optional::isPresent)
				.map(Optional::get);
		*///?}
	}

	//? if >=1.21 {
	private static Integer[] getColorFromFireworkExplosionStack(FireworkExplosion component) {
		Integer[] colors = new Integer[component.colors().size()];

		for (int i = 0; i < component.colors().size(); i++) {
			colors[i] = notZeroAlpha(component.colors().getInt(i));
		}

		return colors;
	}
	//?} else {
	/*public static Optional<Integer[]> getColorFromFireworkExplosionStack(Tag element) {
		return Optional.ofNullable(element)
				.to(CompoundTag.class)
				.to("Colors", IntArrayTag.class)
				.toEmpty(false)
				.map((l) -> l.stream()
						.map((IntTag::getAsInt))
						.map(NbtUtils::notZeroAlpha)
						.toArray(Integer[]::new));
	}
	*///?}

	public static Optional<Integer[]> getColorFromPotionContentsStack(ItemStack stack) {
		//? if >=1.21 {
		PotionContents component = stack.getComponents().get(DataComponents.POTION_CONTENTS);
		if (component != null) {
			Optional<Integer> optional = component.customColor();
			if (optional.isPresent()) {
				return Optional.of(new Integer[]{optional.get()});
			}
		}
		if (component == null) {
			return Optional.empty();
		}
		Iterable<MobEffectInstance> effects = component.getAllEffects();
		List<Integer> colors = new ArrayList<>();
		effects.forEach((effect) -> {
			colors.add(notZeroAlpha(effect.getEffect().value().getColor()));
		});
		if (colors.isEmpty()) {
			return Optional.of(new Integer[]{-13083194});
		}
		return Optional.of(colors.toArray(Integer[]::new));
		//?} else {
		/*return Optional.ofNullable(stack.getTag())
				.map(NbtUtils::getColorFromPotionNbt)
				.filter(Optional::isPresent)
				.map(Optional::get);
		*///?}
	}

	public static Optional<Integer[]> getColorFromDyedStack(ItemStack stack) {
		//? if >=1.21 {
		return Optional.ofNullable(stack.getComponents().get(DataComponents.DYED_COLOR))
				.map(DyedItemColor::rgb)
				.map(NbtUtils::notZeroAlpha)
				.map((i) -> new Integer[]{i});
		//?} else {
		/*return Optional.ofNullable(stack.getTag())
				.to("display", CompoundTag.class)
				.to("color", IntTag.class)
				.map(IntTag::getAsInt)
				.map(NbtUtils::notZeroAlpha)
				.map((i) -> new Integer[]{i});
		*///?}
	}

	//? if <=1.20.1 {
	/*public static Optional<Integer[]> getColorFromPotionNbt(CompoundTag compound) {
		Optional<List<MobEffectInstance>> optional = Optional.ofNullable(compound.get("Potion"))
				.to(StringTag.class)
				.map(Tag::getAsString)
				.map(ResourceLocation::new)
				.map(BuiltInRegistries.POTION::get)
				.map(Potion::getEffects);
		return optional.isPresent() && optional.get().isEmpty() ? NO_EFFECTS_COLOR : optional
				.map(EffectUtils::mixColors)
				.map((o) -> o.map(NbtUtils::notZeroAlpha))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map((i) -> new Integer[]{i});
	}
	*///?}

	private static int notZeroAlpha(int color) {
		int alpha = ArgbUtils2.getAlpha(color);
		if (alpha == 0) {
			return ArgbUtils2.fullAlpha(color);
		}
		return color;
	}

}
