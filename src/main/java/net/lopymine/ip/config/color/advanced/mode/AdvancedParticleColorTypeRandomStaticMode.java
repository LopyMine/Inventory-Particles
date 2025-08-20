package net.lopymine.ip.config.color.advanced.mode;

import lombok.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedParticleColorTypeRandomStaticMode implements IAdvancedParticleColorTypeMode {

	@Nullable
	private Integer currentColor;

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeRandomStaticMode();
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
		if (this.currentColor == null ) {
			this.currentColor = compiledColors[random.nextBetween(0, compiledColors.length - 1)];
		}
		return this.currentColor;
	}

	@Override
	public String asString() {
		return "random_static";
	}
}
