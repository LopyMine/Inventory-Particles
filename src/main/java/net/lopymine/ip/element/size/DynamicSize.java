package net.lopymine.ip.element.size;

import com.mojang.serialization.Codec;
import lombok.*;
import static net.lopymine.mossylib.utils.CodecUtils.option;

import com.mojang.serialization.codecs.RecordCodecBuilder;

@Getter
@Setter
@AllArgsConstructor
public class DynamicSize {

	public static final DynamicSize STANDARD_SIZE = new DynamicSize();

	public static final Codec<DynamicSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("interpolation", DynamicSizeInterpolation.NO_INTERPOLATION, DynamicSizeInterpolation.CODEC, DynamicSize::getInterpolation),
			option("width", Codec.DOUBLE, DynamicSize::getWidth),
			option("height", Codec.DOUBLE, DynamicSize::getHeight)
	).apply(instance, DynamicSize::new));

	private DynamicSizeInterpolation interpolation;
	private double width;
	private double height;

	public DynamicSize(double width, double height) {
		this.interpolation = DynamicSizeInterpolation.NO_INTERPOLATION;
		this.width  = width;
		this.height = height;
	}

	public DynamicSize() {
		this.interpolation = DynamicSizeInterpolation.NO_INTERPOLATION;
		this.width  = 8F;
		this.height = 8F;
	}

}
