package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.util.RandomSource;

@Getter
@Setter
public class AdvancedColorProviderGradient extends TickElement implements IAdvancedColorProvider {

	protected int time;

	public AdvancedColorProviderGradient(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderGradient(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
		if (compiledColors.length == 1) {
			return compiledColors[0];
		}

		if (this.ticks >= this.time - 1) {
			return compiledColors[compiledColors.length-1];
		}

		this.tick();

		float progress = (float) (this.ticks % this.time) / (float) this.time;
		int totalSegments = compiledColors.length - 1;

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
