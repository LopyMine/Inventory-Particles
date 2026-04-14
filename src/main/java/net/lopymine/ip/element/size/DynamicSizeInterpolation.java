package net.lopymine.ip.element.size;

import com.google.common.base.CaseFormat;
import com.mojang.serialization.Codec;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.utils.easing.Easing;
import net.minecraft.util.Mth;

@Getter
@Setter
@AllArgsConstructor
public class DynamicSizeInterpolation {

	private static final Map<String, Function<Double, Double>> REGISTERED_INTERPOLATIONS = new HashMap<>();
	public static final DynamicSizeInterpolation NO_INTERPOLATION = new DynamicSizeInterpolation("shouldn't be registered", (f) -> 1.0D);

	public static final Function<Double, Double> LINEAR_FUNCTION = (f) -> f;
	public static final DynamicSizeInterpolation LINEAR_INTERPOLATION = new DynamicSizeInterpolation("linear", LINEAR_FUNCTION);
	public static final Codec<DynamicSizeInterpolation> CODEC;

	private String id;
	@HideInDebugRender
	private Function<Double, Double> function;

	private static void register(String id, Function<Double, Double> function) {
		REGISTERED_INTERPOLATIONS.put(id, function);
	}

	public double getInterpolated(double first, double second, double progress) {
		return Mth.lerp(this.function.apply(progress), first, second);
	}

	static {
		register(LINEAR_INTERPOLATION.id, LINEAR_INTERPOLATION.function);

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		for (Method method : Easing.class.getDeclaredMethods()) {
			if (method.getReturnType() != Double.TYPE || method.getParameterCount() != 1 || method.getParameters()[0].getType() != Double.TYPE) {
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
						MethodType.methodType(Double.class, Double.class)
				);

				@SuppressWarnings("unchecked")
				Function<Double, Double> function = (Function<Double, Double>) site.getTarget().invokeExact();

				register(name, function);
			} catch (Throwable e) {
				InventoryParticlesClient.LOGGER.error("Failed to bind easing method \"{}\" for particle size interpolation!", name, e);
			}
		}

		CODEC = Codec.STRING.xmap((s) -> {
			Function<Double, Double> function = REGISTERED_INTERPOLATIONS.get(s);
			if (function == null) {
				InventoryParticlesClient.LOGGER.error("Failed to find interpolation method with name \"{}\"!", s);
				return NO_INTERPOLATION;
			}
			return new DynamicSizeInterpolation(s, function);
		}, (i) -> i.id);
	}

}



