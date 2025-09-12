package net.lopymine.ip.config.i2o;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.config.particle.*;
import org.jetbrains.annotations.NotNull;

@Getter
public class Integer2DynamicParticleSize extends Integer2Object<DynamicParticleSize> implements Comparable<Integer2DynamicParticleSize> {

	public static final Codec<Integer2DynamicParticleSize> CODEC = Integer2Object.getCodec(
			"size",
			DynamicParticleSize.STANDARD_SIZE,
			DynamicParticleSize.CODEC,
			Integer2DynamicParticleSize::new
	);

	public Integer2DynamicParticleSize(int index, DynamicParticleSize object) {
		super(index, object);
	}

	public static Integer2DynamicParticleSize fromStatic(StaticParticleSize size) {
		return new Integer2DynamicParticleSize(-1, new DynamicParticleSize(size.getWidth(), size.getHeight()));
	}

	@Override
	public int compareTo(@NotNull Integer2DynamicParticleSize o) {
		return Integer.compare(this.getIndex(), o.getIndex());
	}
}
