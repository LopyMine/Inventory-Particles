package net.lopymine.ip.utils;

import java.util.*;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectUtils {

	//? if <=1.20.1 {
	/*public static Optional<Integer> mixColors(Iterable<MobEffectInstance> effects) {
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;

		for (MobEffectInstance statusEffectInstance : effects) {
			if (statusEffectInstance.isVisible()) {
				int m = statusEffectInstance.getEffect().getColor();
				int n = statusEffectInstance.getAmplifier() + 1;
				i += n * ArgbUtils2.getRed(m);
				j += n * ArgbUtils2.getGreen(m);
				k += n * ArgbUtils2.getBlue(m);
				l += n;
			}
		}

		if (l == 0) {
			return Optional.empty();
		} else {
			return Optional.of(ArgbUtils2.getArgb(255, i / l, j / l, k / l));
		}
	}
	*///?}

}
