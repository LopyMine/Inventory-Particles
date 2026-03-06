package net.lopymine.ip.element.size;

import lombok.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Getter
@Setter
@AllArgsConstructor
public class StaticSize {

	public static final StaticSize STANDARD_SIZE = new StaticSize();

	public static final Codec<StaticSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("width").forGetter(StaticSize::getWidth),
			Codec.DOUBLE.fieldOf("height").forGetter(StaticSize::getHeight)
	).apply(instance, StaticSize::new));

	private double width;
	private double height;

	public StaticSize() {
		this.width  = 8D;
		this.height = 8D;
	}

}
