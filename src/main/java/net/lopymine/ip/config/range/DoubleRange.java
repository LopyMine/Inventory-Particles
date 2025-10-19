package net.lopymine.ip.config.range;

import com.mojang.serialization.Codec;
import java.util.List;
import lombok.*;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
@AllArgsConstructor
public class DoubleRange {

	public static final Codec<DoubleRange> CODEC = Codec.DOUBLE.listOf(/*? if >=1.21 {*/ /*2, 2 *//*?}*/).xmap(DoubleRange::new, DoubleRange::toList);

	private double min;
	private double max;

	public DoubleRange(List<Double> list) {
		this.min = list.get(0);
		this.max = list.get(1);
	}

	public DoubleRange() {
		this.min = 0;
		this.max = 0;
	}

	public double getRandom(Random random) {
		return this.min + ((this.max - this.min) * random.nextDouble());
	}

	public List<Double> toList() {
		return List.of(this.min, this.max);
	}

}
