package net.lopymine.ip.color.advanced.mode;

import lombok.Getter;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
public class AdvancedParticleColorTypeMixedMode implements IAdvancedParticleColorTypeMode {

	@Nullable
	private Integer mixedColor;

	@Override
	public IAdvancedParticleColorTypeMode copy() {
		return new AdvancedParticleColorTypeMixedMode();
	}

	@Override
	public int tickResolve(Integer[] compiledColors, Random random) {
		if (this.mixedColor == null) {
			this.mixedColor = ArgbUtils.mix(compiledColors);
		}
		return this.mixedColor;
	}

	@Override
	public String asString() {
		return "mixed";
	}
}
