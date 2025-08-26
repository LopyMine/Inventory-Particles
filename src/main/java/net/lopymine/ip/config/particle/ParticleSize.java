package net.lopymine.ip.config.particle;

import lombok.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static net.lopymine.ip.utils.CodecUtils.option;
@Setter
@Getter
@AllArgsConstructor
public class ParticleSize {

	public static final ParticleSize STANDARD_SIZE = new ParticleSize();

	public static final Codec<ParticleSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("width",8F, Codec.FLOAT, ParticleSize::getWidth),
			option("height",8F, Codec.FLOAT, ParticleSize::getHeight)
	).apply(instance, ParticleSize::new));

	private float width;
	private float height;

	public ParticleSize() {
		this.width  = 8F;
		this.height = 8F;
	}

}
