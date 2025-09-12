package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import lombok.*;
import static net.lopymine.ip.utils.CodecUtils.option;

import com.mojang.serialization.codecs.RecordCodecBuilder;

@Getter
@Setter
@AllArgsConstructor
public class DynamicParticleSize {

	public static final DynamicParticleSize STANDARD_SIZE = new DynamicParticleSize();

	public static final Codec<DynamicParticleSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("interpolation", DynamicParticleSizeInterpolation.NO_INTERPOLATION, DynamicParticleSizeInterpolation.CODEC, DynamicParticleSize::getInterpolation),
			option("width", Codec.DOUBLE, DynamicParticleSize::getWidth),
			option("height", Codec.DOUBLE, DynamicParticleSize::getHeight)
	).apply(instance, DynamicParticleSize::new));

	private DynamicParticleSizeInterpolation interpolation;
	private double width;
	private double height;

	public DynamicParticleSize(double width, double height) {
		this.interpolation = DynamicParticleSizeInterpolation.NO_INTERPOLATION;
		this.width  = width;
		this.height = height;
	}

	public DynamicParticleSize() {
		this.interpolation = DynamicParticleSizeInterpolation.NO_INTERPOLATION;
		this.width  = 8F;
		this.height = 8F;
	}

}
