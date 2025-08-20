package net.lopymine.ip.config.color.advanced.mode;

import net.lopymine.ip.element.base.TickElement;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.util.math.random.Random;

public class AdvancedParticleColorTypeGradientMode extends TickElement implements IAdvancedParticleColorTypeMode {

	protected final int time;

	public AdvancedParticleColorTypeGradientMode(int time) {
		this.time = time;
	}

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeGradientMode(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
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

		return ArgbUtils.lerp(currentSegmentProgress, first, second);
	}

	@Override
	public String asString() {
		return "gradient";
	}
}
