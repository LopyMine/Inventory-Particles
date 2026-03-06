package net.lopymine.ip.config.i2o;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.element.size.*;
import org.jetbrains.annotations.NotNull;

@Getter
public class Integer2DynamicParticleSize extends Integer2Object<DynamicSize> implements Comparable<Integer2DynamicParticleSize> {

	public static final Codec<Integer2DynamicParticleSize> CODEC = Integer2Object.getCodec(
			"size",
			DynamicSize.STANDARD_SIZE,
			DynamicSize.CODEC,
			Integer2DynamicParticleSize::new
	);

	public Integer2DynamicParticleSize(int index, DynamicSize object) {
		super(index, object);
	}

	public static Integer2DynamicParticleSize fromStatic(StaticSize size) {
		return new Integer2DynamicParticleSize(-1, new DynamicSize(size.getWidth(), size.getHeight()));
	}

	@Override
	public int compareTo(@NotNull Integer2DynamicParticleSize o) {
		return Integer.compare(this.getIndex(), o.getIndex());
	}
}
