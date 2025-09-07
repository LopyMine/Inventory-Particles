package net.lopymine.ip.color.advanced.mode;

import java.util.Map;
import net.lopymine.ip.debug.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public interface IAdvancedParticleColorTypeMode extends IDebugRenderable {

	@HideInDebugRender
	Map<String, AdvancedParticleColorTypeModeFactory> FACTORIES = Map.of(
			"random", (s, speed) -> new AdvancedParticleColorTypeRandomMode(speed),
			"random_static", (s, speed) -> new AdvancedParticleColorTypeRandomStaticMode(),
			"gradient", (s, speed) -> new AdvancedParticleColorTypeGradientMode(speed),
			"gradient_random_static", (s, speed) -> new AdvancedParticleColorTypeGradientRandomStaticMode(speed),
			"gradient_loop", (s, speed) -> new AdvancedParticleColorTypeGradientLoopMode(speed),
			"gradient_bounce", (s, speed) -> new AdvancedParticleColorTypeGradientBounceMode(speed),
			"mixed", (s, speed) -> new AdvancedParticleColorTypeMixedMode()
	);

	int tickResolve(Integer[] compiledColors, Random random);

	String asString();

	IAdvancedParticleColorTypeMode copy();

	static IAdvancedParticleColorTypeMode parse(String s, int speed) {
		AdvancedParticleColorTypeModeFactory factory = FACTORIES.get(s);
		if (factory == null) {
			return new AdvancedParticleColorTypeRandomStaticMode();
		}
		return factory.create(s, speed);
	}

	interface AdvancedParticleColorTypeModeFactory {

		@NotNull IAdvancedParticleColorTypeMode create(String s, int speed);

	}

}
