package net.lopymine.ip.color.advanced.mode;

import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.util.math.random.Random;

public class AdvancedParticleColorTypeGradientLoopMode extends TickElement implements IAdvancedParticleColorTypeMode {

	protected final int time;

	public AdvancedParticleColorTypeGradientLoopMode(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeGradientLoopMode(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
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

		return ArgbUtils.lerp(currentSegmentProgress, first, second);
	}

	@Override
	public String asString() {
		return "gradient_loop";
	}
}