package net.lopymine.ip.color.advanced.mode;

import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class AdvancedParticleColorTypeGradientRandomStaticMode extends AdvancedParticleColorTypeGradientMode {

	@Nullable
	private Integer color;

	public AdvancedParticleColorTypeGradientRandomStaticMode(int time) {
		super(time);
	}

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeGradientRandomStaticMode(this.time);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
		if (this.color == null) {
			this.ticks = random.nextBetween(0, this.time);
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
