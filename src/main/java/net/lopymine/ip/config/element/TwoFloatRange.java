package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import java.util.List;
import lombok.*;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
@AllArgsConstructor
public class TwoFloatRange {

	public static final Codec<TwoFloatRange> CODEC = Codec.FLOAT.listOf(2, 2).xmap(TwoFloatRange::new, TwoFloatRange::toList);

	private float min;
	private float max;

	public TwoFloatRange(List<Float> list) {
		this.min = list.get(0);
		this.max = list.get(1);
	}

	public TwoFloatRange() {
		this.min = 0;
		this.max = 0;
	}

	public float getRandom(Random random) {
		return this.min + ((this.max - this.min) * random.nextFloat());
	}

	public List<Float> toList() {
		return List.of(this.min, this.max);
	}

}
