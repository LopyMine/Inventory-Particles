package net.lopymine.ip.color.advanced.mode;

import lombok.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedParticleColorTypeRandomMode extends AbstractAdvancedParticleColorTypeModeWithPeriod {

	@Nullable
	private Integer currentColor;

	public AdvancedParticleColorTypeRandomMode(float speed) {
		super(speed);
	}

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeRandomMode(this.speed);
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
		this.tick();

		if (this.currentColor != null && this.ticks < this.changeColorTick) {
			return this.currentColor;
		}

		this.updateChangeColorTick();

		return this.currentColor = compiledColors[random.nextBetween(0, compiledColors.length - 1)];
	}

	@Override
	public String asString() {
		return "random";
	}
}
