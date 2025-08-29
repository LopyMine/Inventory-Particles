package net.lopymine.ip.config.particle;

import lombok.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Setter
@Getter
@AllArgsConstructor
public class StaticParticleSize {

	public static final StaticParticleSize STANDARD_SIZE = new StaticParticleSize();

	public static final Codec<StaticParticleSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("width").forGetter(StaticParticleSize::getWidth),
			Codec.FLOAT.fieldOf("height").forGetter(StaticParticleSize::getHeight)
	).apply(instance, StaticParticleSize::new));

	private float width;
	private float height;

	public StaticParticleSize() {
		this.width  = 8F;
		this.height = 8F;
	}

}
