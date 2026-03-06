package net.lopymine.ip.element.color.advanced;

import lombok.*;
import net.lopymine.ip.utils.ArgbUtils2;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class AdvancedColorProviderMixed implements IAdvancedColorProvider {

	@Nullable
	private Integer mixedColor;

	@Override
	public IAdvancedColorProvider copy() {
		return new AdvancedColorProviderMixed();
	}

	@Override
	public int tickResolve(Integer[] compiledColors, RandomSource random) {
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
