package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedColorProviderRandomStatic implements IAdvancedColorProvider {

	@Nullable
	private Integer currentColor;

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderRandomStatic();
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
