package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.util.RandomSource;

@Getter
@Setter
public class AdvancedColorProviderGradientLoop extends TickElement implements IAdvancedColorProvider {

	protected final int time;

	public AdvancedColorProviderGradientLoop(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderGradientLoop(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		if (compiledColors.length == 1) {
			return compiledColors[0];
		}

		this.tick();

		float progress = (float) (this.ticks % this.time) / (float) this.time;
		int totalSegments = compiledColors.length;

		float segmentProgress = progress * totalSegments;
		int segmentIndex = (int) (double) segmentProgress;

		float currentSegmentProgress = segmentProgress - segmentIndex;

		int secondColorIndex = segmentIndex + 1;
		if (secondColorIndex >= totalSegments) {
			secondColorIndex = 0;
		}

		int first = compiledColors[segmentIndex];
		int second = compiledColors[secondColorIndex];

		return ArgbUtils2.lerp(currentSegmentProgress, first, second);
	}

	@Override
	public String asString() {
		return "gradient_loop";
	}
}