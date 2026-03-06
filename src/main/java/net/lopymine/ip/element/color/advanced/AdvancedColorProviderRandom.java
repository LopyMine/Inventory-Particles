package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedColorProviderRandom extends AbstractAdvancedColorProviderWithPeriod {

	@Nullable
	private Integer currentColor;

	public AdvancedColorProviderRandom(float speed) {
		super(speed);
	}

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderRandom(this.speed);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		this.tick();

		if (this.currentColor != null && this.ticks < this.changeColorTick) {
			return this.currentColor;
		}

		this.updateChangeColorTick();

		return this.currentColor = compiledColors[random.nextIntBetweenInclusive(0, compiledColors.length - 1)];
	}

	@Override
	public String asString() {
		return "random";
	}
}
