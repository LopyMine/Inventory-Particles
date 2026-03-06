package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.util.RandomSource;

@Getter
@Setter
public class AdvancedColorProviderGradientBounce extends TickElement implements IAdvancedColorProvider {

	protected final int time;

	public AdvancedColorProviderGradientBounce(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderGradientBounce(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		if (compiledColors.length == 1) {
			return compiledColors[0];
		}

		this.tick();

		int totalSegments = compiledColors.length - 1;
		int cycle = this.time * 2;
		int phase = this.ticks % cycle;

		float progress;
		if (phase < this.time) {
			progress = (float) phase / (float) this.time;
		} else {
			progress = 1.0f - ((float) (phase - this.time) / (float) this.time);
		}

		float segmentProgress = progress * totalSegments;
		int segmentIndex = (int) (double) segmentProgress;

		if (segmentIndex >= totalSegments) {
			segmentIndex = totalSegments - 1;
			segmentProgress = totalSegments;
		}

		float currentSegmentProgress = segmentProgress - segmentIndex;

		int first = compiledColors[segmentIndex];
		int second = compiledColors[segmentIndex + 1];

		return ArgbUtils2.lerp(currentSegmentProgress, first, second);
	}

	@Override
	public String asString() {
		return "gradient";
	}
}
