package net.lopymine.ip.config.particle;

import lombok.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Getter
@Setter
@AllArgsConstructor
public class StaticParticleSize {

	public static final StaticParticleSize STANDARD_SIZE = new StaticParticleSize();

	public static final Codec<StaticParticleSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("width").forGetter(StaticParticleSize::getWidth),
			Codec.DOUBLE.fieldOf("height").forGetter(StaticParticleSize::getHeight)
	).apply(instance, StaticParticleSize::new));

	private double width;
	private double height;

	public StaticParticleSize() {
		this.width  = 8D;
		this.height = 8D;
	}

}
