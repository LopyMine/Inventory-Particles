package net.lopymine.ip.color.advanced.mode;

import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.util.math.random.Random;

public class AdvancedParticleColorTypeGradientBounceMode extends TickElement implements IAdvancedParticleColorTypeMode {

	protected final int time;

	public AdvancedParticleColorTypeGradientBounceMode(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeGradientBounceMode(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
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

		return ArgbUtils.lerp(currentSegmentProgress, first, second);
	}

	@Override
	public String asString() {
		return "gradient";
	}
}
