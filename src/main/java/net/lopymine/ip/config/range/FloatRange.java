package net.lopymine.ip.config.range;

import com.mojang.serialization.Codec;
import java.util.List;
import lombok.*;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
@AllArgsConstructor
public class FloatRange {

	public static final Codec<FloatRange> CODEC = Codec.FLOAT.listOf(/*? if >=1.21 {*/ 2, 2 /*?}*/).xmap(FloatRange::new, FloatRange::toList);

	private float min;
	private float max;

	public FloatRange(List<Float> list) {
		this.min = list.get(0);
		this.max = list.get(1);
	}

	public FloatRange() {
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
