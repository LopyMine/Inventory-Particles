package net.lopymine.ip.config.particle;

import com.google.common.base.CaseFormat;
import com.mojang.serialization.Codec;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.utils.Easing;
import net.minecraft.util.math.MathHelper;

@Getter
@Setter
@AllArgsConstructor
public class DynamicParticleSizeInterpolation {

	private static final Map<String, Function<Float, Float>> REGISTERED_INTERPOLATIONS = new HashMap<>();
	public static final DynamicParticleSizeInterpolation NO_INTERPOLATION = new DynamicParticleSizeInterpolation("shouldn't be registered", (f) -> 1.0F);

	public static final Function<Float, Float> LINEAR_FUNCTION = (f) -> f;
	public static final DynamicParticleSizeInterpolation LINEAR_INTERPOLATION = new DynamicParticleSizeInterpolation("linear", LINEAR_FUNCTION);
	public static final Codec<DynamicParticleSizeInterpolation> CODEC;

	private String id;
	@HideInDebugRender
	private Function<Float, Float> function;

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
			Function<Float, Float> function = REGISTERED_INTERPOLATIONS.get(s);
			if (function == null) {
				InventoryParticlesClient.LOGGER.error("Failed to find interpolation method with name \"{}\"!", s);
				return NO_INTERPOLATION;
			}
			return new DynamicParticleSizeInterpolation(s, function);
		}, (i) -> i.id);
	}

}



