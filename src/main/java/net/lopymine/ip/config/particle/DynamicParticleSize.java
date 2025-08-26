package net.lopymine.ip.config.particle;

import com.google.common.base.CaseFormat;
import com.mojang.serialization.Codec;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.misc.Integer2ParticleSize;
import net.minecraft.util.math.MathHelper;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public class DynamicParticleSize {

	public static DynamicParticleSize STANDARD = new DynamicParticleSize(List.of(), DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION);

	private List<Integer2ParticleSize> sizes;
	private DynamicParticleSizeInterpolation interpolation;

	public static DynamicParticleSize single(ParticleSize size) {
		return new DynamicParticleSize(List.of(Integer2ParticleSize.standard(size)), DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION);
	}

	public static final Codec<List<Integer2ParticleSize>> SIZES_CODEC = Codec.unboundedMap(Codec.STRING, ParticleSize.CODEC).xmap((map) -> {
		List<Integer2ParticleSize> list = new ArrayList<>(map.entrySet().stream().map((entry) -> new Integer2ParticleSize(Integer.parseInt(entry.getKey()), entry.getValue())).toList());
		Collections.sort(list);
		return list;
	}, (list) -> list.stream().collect(Collectors.toMap((size) -> String.valueOf(size.getIndex()), Integer2ParticleSize::getObject)));

	public static final Codec<DynamicParticleSize> CODEC = create((instance) -> instance.group(
			option("sizes", new ArrayList<>(), SIZES_CODEC, DynamicParticleSize::getSizes),
			option("interpolation", DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION, DynamicParticleSizeInterpolation.CODEC, DynamicParticleSize::getInterpolation)
	).apply(instance, DynamicParticleSize::new));

	@AllArgsConstructor
	public static class DynamicParticleSizeInterpolation {

		public static final Function<Float, Float> LINEAR = (f) -> f;
		private static final DynamicParticleSizeInterpolation LINEAR_INTERPOLATION = new DynamicParticleSizeInterpolation("linear", LINEAR);
		private static final Map<String, Function<Float, Float>> REGISTERED_INTERPOLATIONS = new HashMap<>();
		private static final Codec<DynamicParticleSizeInterpolation> CODEC;

		private final String id;
		private final Function<Float, Float> function;

		private static void register(String id, Function<Float, Float> function) {
			REGISTERED_INTERPOLATIONS.put(id, function);
		}

		public float getInterpolated(float first, float second, float progress) {
			return MathHelper.lerp(this.function.apply(progress), first, second);
		}

		static {
			register(LINEAR_INTERPOLATION.id, LINEAR_INTERPOLATION.function);

			MethodHandles.Lookup lookup = MethodHandles.lookup();
			for (Method method : Easing.class.getDeclaredMethods()) {
				if (method.getReturnType() != Float.TYPE || method.getParameterCount() != 1 || method.getParameters()[0].getType() != Float.TYPE) {
					continue;
				}
				String name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, method.getName());

				try {
					MethodHandle methodHandle = lookup.unreflect(method);

					CallSite site = LambdaMetafactory.metafactory(
							lookup,
							"apply",
							MethodType.methodType(Function.class),
							MethodType.methodType(Object.class, Object.class),
							methodHandle,
							MethodType.methodType(Float.class, Float.class)
					);

					@SuppressWarnings("unchecked")
					Function<Float, Float> function = (Function<Float, Float>) site.getTarget().invokeExact();

					register(name, function);
				} catch (Throwable e) {
					InventoryParticlesClient.LOGGER.error("Failed to bind easing method \"{}\" for particle size interpolation!", name, e);
				}
			}

			CODEC = Codec.STRING.xmap((s) -> {
				Function<Float, Float> function = REGISTERED_INTERPOLATIONS.getOrDefault(s, LINEAR);
				return new DynamicParticleSizeInterpolation(s, function);
			}, (i) -> i.id);
		}

	}

	// https://nicmulvaney.com/easing
	@SuppressWarnings("unused")
	public static final class Easing {

		public static float easeInSine(float x) {
			return (float) (1 - Math.cos((x * Math.PI) / 2));
		}

		public static float easeOutSine(float x) {
			return (float) Math.sin((x * Math.PI) / 2);
		}

		public static float easeInOutSine(float x) {
			return (float) (-(Math.cos(Math.PI * x) - 1) / 2);
		}

		public static float easeInQuad(float x) {
			return x * x;
		}

		public static float easeOutQuad(float x) {
			return 1 - (1 - x) * (1 - x);
		}

		public static float easeInOutQuad(float x) {
			return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
		}

		public static float easeInCubic(float x) {
			return x * x * x;
		}

		public static float easeOutCubic(float x) {
			return (float) (1 - Math.pow(1 - x, 3));
		}

		public static float easeInOutCubic(float x) {
			return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
		}

		public static float easeInQuart(float x) {
			return x * x * x * x;
		}

		public static float easeOutQuart(float x) {
			return (float) (1 - Math.pow(1 - x, 4));
		}

		public static float easeInOutQuart(float x) {
			return (float) (x < 0.5 ? 8 * Math.pow(x, 4) : 1 - Math.pow(-2 * x + 2, 4) / 2);
		}

		public static float easeInQuint(float x) {
			return x * x * x * x * x;
		}

		public static float easeOutQuint(float x) {
			return (float) (1 - Math.pow(1 - x, 5));
		}

		public static float easeInOutQuint(float x) {
			return (float) (x < 0.5 ? 16 * Math.pow(x, 5) : 1 - Math.pow(-2 * x + 2, 5) / 2);
		}

		public static float easeInExpo(float x) {
			return x == 0 ? 0 : (float) Math.pow(2, 10 * x - 10);
		}

		public static float easeOutExpo(float x) {
			return x == 1 ? 1 : (float) (1 - Math.pow(2, -10 * x));
		}

		public static float easeInOutExpo(float x) {
			if (x == 0) return 0;
			if (x == 1) return 1;
			return (float) (x < 0.5
								? Math.pow(2, 20 * x - 10) / 2
								: (2 - Math.pow(2, -20 * x + 10)) / 2);
		}

		public static float easeInCirc(float x) {
			return (float) (1 - Math.sqrt(1 - Math.pow(x, 2)));
		}

		public static float easeOutCirc(float x) {
			return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
		}

		public static float easeInOutCirc(float x) {
			return (float) (x < 0.5
								? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
								: (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
		}

		public static float easeInBack(float x) {
			final double c1 = 1.70158;
			final double c3 = c1 + 1;
			return (float) (c3 * x * x * x - c1 * x * x);
		}

		public static float easeOutBack(float x) {
			final double c1 = 1.70158;
			final double c3 = c1 + 1;
			return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
		}

		public static float easeInOutBack(float x) {
			final double c1 = 1.70158;
			final double c2 = c1 * 1.525;
			if (x < 0.5) {
				return (float) ((Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2);
			} else {
				return (float) ((Math.pow(2 * x - 2, 2) * ((c2 + 1) * (2 * x - 2) + c2) + 2) / 2);
			}
		}

		public static float easeInElastic(float x) {
			final double c4 = (2 * Math.PI) / 3;
			if (x == 0) return 0;
			if (x == 1) return 1;
			return (float) (-Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4));
		}

		public static float easeOutElastic(float x) {
			final double c4 = (2 * Math.PI) / 3;
			if (x == 0) return 0;
			if (x == 1) return 1;
			return (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
		}

		public static float easeInOutElastic(float x) {
			final double c5 = (2 * Math.PI) / 4.5;
			if (x == 0) return 0;
			if (x == 1) return 1;
			double sin = Math.sin((20 * x - 11.125) * c5);
			return (float) (x < 0.5
								? -(Math.pow(2, 20 * x - 10) * sin) / 2
								: (Math.pow(2, -20 * x + 10) * sin) / 2 + 1);
		}

		public static float easeInBounce(float x) {
			return 1 - easeOutBounce(1 - x);
		}

		public static float easeOutBounce(float x) {
			final double n1 = 7.5625;
			final double d1 = 2.75;
			double t = x;
			if (t < 1 / d1) {
				return (float) (n1 * t * t);
			} else if (t < 2 / d1) {
				t -= 1.5 / d1;
				return (float) (n1 * t * t + 0.75);
			} else if (t < 2.5 / d1) {
				t -= 2.25 / d1;
				return (float) (n1 * t * t + 0.9375);
			} else {
				t -= 2.625 / d1;
				return (float) (n1 * t * t + 0.984375);
			}
		}

		public static float easeInOutBounce(float x) {
			return x < 0.5
					? (1 - easeOutBounce(1 - 2 * x)) / 2
					: (1 + easeOutBounce(2 * x - 1)) / 2;
		}
	}


}

