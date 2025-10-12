package net.lopymine.ip.color.advanced.mode;

import lombok.*;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
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
			this.mixedColor = ArgbUtils2.mix(compiledColors);
		}
		return this.mixedColor;
	}

	@Override
	public String asString() {
		return "mixed";
	}
}
