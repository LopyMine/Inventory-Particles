package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedColorProviderGradientRandomStatic extends AdvancedColorProviderGradient {

	@Nullable
	private Integer color;

	public AdvancedColorProviderGradientRandomStatic(int time) {
		super(time);
	}

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderGradientRandomStatic(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		if (this.color == null) {
			this.ticks = random.nextIntBetweenInclusive(0, this.time);
			this.color = super.tickResolve(compiledColors, random);
		}
		return this.color;
	}

	@Override
	public void tick() {
	}

	@Override
	public String asString() {
		return "gradient_random_static";
	}
}
