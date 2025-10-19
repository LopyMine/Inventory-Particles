package net.lopymine.ip.config.range;

import com.mojang.serialization.Codec;
import java.util.List;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class IntegerRange {

	public static final Codec<IntegerRange> CODEC = Codec.INT.listOf(/*? if >=1.21 {*/ /*2, 2 *//*?}*/).xmap(IntegerRange::new, IntegerRange::toList);

	private int min;
	private int max;

	public IntegerRange(List<Integer> list) {
		this.min = list.get(0);
		this.max = list.get(1);
	}

	public IntegerRange() {
		this.min = 0;
		this.max = 0;
	}

	public List<Integer> toList() {
		return List.of(this.min, this.max);
	}

}
