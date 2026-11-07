package net.lopymine.ip.editor.field;

import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.range.*;
import net.lopymine.ip.element.color.*;
import net.lopymine.ip.element.color.advanced.*;
import net.lopymine.ip.element.mod.spawner.ParticleSpawnAreaId;
import net.lopymine.ip.element.size.DynamicSizeInterpolation;
import net.lopymine.ip.element.texture.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class FieldValues {

	public static final String INFINITY = "∞";

	private static final double EPSILON = 1.0E-7D;

	public static boolean isText(Class<?> type) {
		return type == String.class
				|| type == Identifier.class
				|| type == CachedItem.class
				|| type == ParticleSpawnAreaId.class
				|| type == DynamicSizeInterpolation.class
				|| type == Either.class
				|| isNumber(type);
	}

	public static boolean isNumber(Class<?> type) {
		return type == int.class || type == Integer.class
				|| type == double.class || type == Double.class
				|| type == float.class || type == Float.class
				|| type == long.class || type == Long.class;
	}

	public static boolean isBoolean(Class<?> type) {
		return type == boolean.class || type == Boolean.class;
	}

	public static boolean isVariantOnly(Class<?> type) {
		return IAdvancedColorProvider.class.isAssignableFrom(type);
	}

	public static String toText(Class<?> ignored, @Nullable Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof CachedItem cachedItem) {
			return cachedItem.getId().toString();
		}
		if (value instanceof ParticleSpawnAreaId spawnAreaId) {
			return spawnAreaId.asString();
		}
		if (value instanceof DynamicSizeInterpolation interpolation) {
			return isNoInterpolation(interpolation) ? "" : interpolation.getId();
		}
		if (value instanceof IColorProvider colorProvider) {
			return value instanceof AdvancedColorProvider ? "advanced" : colorProvider.asString();
		}
		if (value instanceof Either<?, ?> either) {
			Optional<?> right = either.right();
			return right.map("#%s"::formatted).orElseGet(() -> either.left().map((left) -> toText(left.getClass(), left)).orElse(""));
		}
		if (value instanceof Double number && Math.abs(number) == Double.MAX_VALUE) {
			return number > 0 ? INFINITY : "-" + INFINITY;
		}
		return String.valueOf(value);
	}

	@Nullable
	public static Object fromText(Class<?> type, String text, @Nullable Object previous) {
		try {
			if (type == String.class) {
				return text;
			}
			if (type == int.class || type == Integer.class) {
				return Integer.parseInt(text.trim());
			}
			if (type == long.class || type == Long.class) {
				return Long.parseLong(text.trim());
			}
			if (type == double.class || type == Double.class) {
				String value = text.trim();
				if (value.equals(INFINITY) || value.equals("-" + INFINITY)) {
					return value.startsWith("-") ? -Double.MAX_VALUE : Double.MAX_VALUE;
				}
				return Double.parseDouble(value);
			}
			if (type == float.class || type == Float.class) {
				return Float.parseFloat(text.trim());
			}
			if (type == Identifier.class) {
				return InventoryParticles.parseId(text.trim());
			}
			if (type == CachedItem.class) {
				return new CachedItem(InventoryParticles.parseId(text.trim()));
			}
			if (type == ParticleSpawnAreaId.class) {
				return ParticleSpawnAreaId.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(text.trim()))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/;
			}
			if (type == DynamicSizeInterpolation.class) {
				String id = text.trim();
				return id.isEmpty() ? DynamicSizeInterpolation.NO_INTERPOLATION : DynamicSizeInterpolation.find(id);
			}
			if (IColorProvider.class.isAssignableFrom(type)) {
				return IColorProvider.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(text.trim()))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/;
			}
			if (type == Either.class) {
				String value = text.trim();
				if (value.startsWith("#")) {
					return Either.right(InventoryParticles.parseId(value.substring(1)));
				}
				return Either.left(new CachedItem(InventoryParticles.parseId(value)));
			}
		} catch (Exception e) {
			return null;
		}
		return previous;
	}

	@Nullable
	public static Object stepNumber(Class<?> type, @Nullable Object value, double step, int direction) {
		if (!(value instanceof Number number) || step <= 0.0D || direction == 0) {
			return null;
		}

		boolean integral = type == int.class || type == Integer.class || type == long.class || type == Long.class;
		double stepValue = integral ? Math.max(1.0D, Math.round(step * 10.0D)) : step;

		double steps = number.doubleValue() / stepValue;
		double result = direction > 0
				?
				(Math.floor(steps + EPSILON) + 1.0D) * stepValue
				:
				(Math.ceil(steps - EPSILON) - 1.0D) * stepValue;

		return asNumber(type, result);
	}

	@Nullable
	public static Object asNumber(Class<?> type, double value) {
		if (type == int.class || type == Integer.class) {
			return (int) Math.round(value);
		}
		if (type == long.class || type == Long.class) {
			return Math.round(value);
		}
		if (type == float.class || type == Float.class) {
			return (float) round(value);
		}
		if (type == double.class || type == Double.class) {
			return round(value);
		}
		return null;
	}

	private static double round(double value) {
		return Math.round(value * 1000.0D) / 1000.0D;
	}

	public static boolean isNoInterpolation(DynamicSizeInterpolation interpolation) {
		return interpolation.getId().equals(DynamicSizeInterpolation.NO_INTERPOLATION.getId());
	}

	@Nullable
	public static Object copy(@Nullable Object value) {
		if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Enum<?> || value instanceof Identifier || value instanceof Either<?, ?> || value instanceof DynamicSizeInterpolation) {
			return value;
		}
		if (value instanceof CachedItem cachedItem) {
			return new CachedItem(cachedItem.getId());
		}
		if (value instanceof ParticleSpawnAreaId spawnAreaId) {
			return new ParticleSpawnAreaId(spawnAreaId.getId());
		}
		if (value instanceof IntegerRange range) {
			return new IntegerRange(range.getMin(), range.getMax());
		}
		if (value instanceof DoubleRange range) {
			return new DoubleRange(range.getMin(), range.getMax());
		}
		if (value instanceof IColorProvider colorProvider) {
			return colorProvider.copy();
		}
		if (value instanceof AtlasTexture texture) {
			return new AtlasTexture(texture.getSprite(), texture.getAtlas());
		}
		if (value instanceof ItemTexture texture) {
			return new ItemTexture(new CachedItem(texture.getId()));
		}
		return null;
	}

	public static List<Variant> getVariants(Class<?> type) {
		if (ITexture.class.isAssignableFrom(type)) {
			return List.of(
					new Variant("atlas_texture", AtlasTexture.class, () -> new AtlasTexture(AtlasTexture.NO_SPRITE, null)),
					new Variant("item_texture", ItemTexture.class, () -> new ItemTexture(new CachedItem(InventoryParticles.parseId("minecraft:stone"))))
			);
		}
		if (IAdvancedColorProvider.class.isAssignableFrom(type)) {
			return List.of(
					new Variant("random", AdvancedColorProviderRandom.class, () -> new AdvancedColorProviderRandom(0)),
					new Variant("random_static", AdvancedColorProviderRandomStatic.class, AdvancedColorProviderRandomStatic::new),
					new Variant("gradient", AdvancedColorProviderGradient.class, () -> new AdvancedColorProviderGradient(0)),
					new Variant("gradient_random_static", AdvancedColorProviderGradientRandomStatic.class, () -> new AdvancedColorProviderGradientRandomStatic(0)),
					new Variant("gradient_loop", AdvancedColorProviderGradientLoop.class, () -> new AdvancedColorProviderGradientLoop(0)),
					new Variant("gradient_bounce", AdvancedColorProviderGradientBounce.class, () -> new AdvancedColorProviderGradientBounce(0)),
					new Variant("mixed", AdvancedColorProviderMixed.class, AdvancedColorProviderMixed::new)
			);
		}
		if (IColorProvider.class.isAssignableFrom(type)) {
			return List.of(
					new Variant("standard_color", StandardColorProvider.class, StandardColorProvider::new),
					new Variant("custom_color", CustomColorProvider.class, () -> new CustomColorProvider("#ffffff")),
					new Variant("nbt_color", NbtColorProvider.class, NbtColorProvider::new),
					new Variant("nbt_list_color", NbtListColorProvider.class, NbtListColorProvider::new),
					new Variant("advanced_color", AdvancedColorProvider.class, () -> new AdvancedColorProvider(new AdvancedColorProviderRandomStatic(), new ArrayList<>(), 0))
			);
		}
		return List.of();
	}

	public record Variant(String name, Class<?> type, Factory factory) {

		public interface Factory {

			Object create();

		}
	}
}
