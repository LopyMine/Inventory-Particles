package net.lopymine.ip.element.color.advanced;

import java.util.Map;
import net.lopymine.ip.debug.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public interface IAdvancedColorProvider extends IDebugRenderable {

	@HideInDebugRender
	Map<String, AdvancedParticleColorTypeModeFactory> FACTORIES = Map.of(
			"random", (s, speed) -> new AdvancedColorProviderRandom(speed),
			"random_static", (s, speed) -> new AdvancedColorProviderRandomStatic(),
			"gradient", (s, speed) -> new AdvancedColorProviderGradient(speed),
			"gradient_random_static", (s, speed) -> new AdvancedColorProviderGradientRandomStatic(speed),
			"gradient_loop", (s, speed) -> new AdvancedColorProviderGradientLoop(speed),
			"gradient_bounce", (s, speed) -> new AdvancedColorProviderGradientBounce(speed),
			"mixed", (s, speed) -> new AdvancedColorProviderMixed()
	);

	int tickResolve(Integer[] compiledColors, RandomSource random);

	String asString();

	IAdvancedColorProvider copy();

	static IAdvancedColorProvider parse(String s, int speed) {
		AdvancedParticleColorTypeModeFactory factory = FACTORIES.get(s);
		if (factory == null) {
			return new AdvancedColorProviderRandomStatic();
		}
		return factory.create(s, speed);
	}

	interface AdvancedParticleColorTypeModeFactory {

		@NotNull IAdvancedColorProvider create(String s, int speed);

	}

}
