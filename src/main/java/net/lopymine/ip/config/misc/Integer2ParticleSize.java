package net.lopymine.ip.config.misc;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.config.particle.ParticleSize;
import org.jetbrains.annotations.NotNull;

@Getter
public class Integer2ParticleSize extends Integer2Object<ParticleSize> implements Comparable<Integer2ParticleSize> {

	public static final Codec<Integer2ParticleSize> CODEC = Integer2Object.getCodec(
			"size",
			ParticleSize.STANDARD_SIZE,
			ParticleSize.CODEC,
			Integer2ParticleSize::new
	);

	public Integer2ParticleSize(int index, ParticleSize object) {
		super(index, object);
	}

	public static Integer2ParticleSize standard(ParticleSize size) {
		return new Integer2ParticleSize(-1, size);
	}

	@Override
	public int compareTo(@NotNull Integer2ParticleSize o) {
		return Integer.compare(this.getIndex(), o.getIndex());
	}
}
