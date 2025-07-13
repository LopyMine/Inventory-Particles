package net.lopymine.ip.config.element;

import com.mojang.serialization.Codec;
import java.util.List;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
public class TwoIntegerRange {

	public static final Codec<TwoIntegerRange> CODEC = Codec.INT.listOf(2, 2).xmap(TwoIntegerRange::new, TwoIntegerRange::toList);

	private int min;
	private int max;

	public TwoIntegerRange(List<Integer> list) {
		this.min = list.get(0);
		this.max = list.get(1);
	}

	public TwoIntegerRange() {
		this.min = 0;
		this.max = 0;
	}

	public List<Integer> toList() {
		return List.of(this.min, this.max);
	}

}
