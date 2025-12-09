package net.lopymine.ip.color.advanced.mode;

import lombok.*;
import net.minecraft.util.RandomSource;
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
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		if (this.currentColor == null ) {
			this.currentColor = compiledColors[random.nextIntBetweenInclusive(0, compiledColors.length - 1)];
		}
		return this.currentColor;
	}

	@Override
	public String asString() {
		return "random_static";
	}
}
