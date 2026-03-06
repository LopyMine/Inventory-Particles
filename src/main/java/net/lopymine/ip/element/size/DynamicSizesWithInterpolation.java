package net.lopymine.ip.element.size;

import com.mojang.serialization.Codec;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import net.lopymine.ip.config.i2o.Integer2DynamicParticleSize;
import net.lopymine.ip.debug.IDebugRenderable;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class DynamicSizesWithInterpolation implements IDebugRenderable {

	public static final DynamicSizesWithInterpolation STANDARD = new DynamicSizesWithInterpolation(List.of(), DynamicSizeInterpolation.LINEAR_INTERPOLATION);

	private List<Integer2DynamicParticleSize> sizes;
	private DynamicSizeInterpolation interpolation;

	public static DynamicSizesWithInterpolation fromStatic(StaticSize size) {
		return new DynamicSizesWithInterpolation(List.of(Integer2DynamicParticleSize.fromStatic(size)), DynamicSizeInterpolation.LINEAR_INTERPOLATION);
	}

	public static final Codec<List<Integer2DynamicParticleSize>> SIZES_CODEC = Codec.unboundedMap(Codec.STRING, DynamicSize.CODEC).xmap((map) -> {
		List<Integer2DynamicParticleSize> list = new ArrayList<>(map.entrySet().stream().map((entry) -> new Integer2DynamicParticleSize(Integer.parseInt(entry.getKey()), entry.getValue())).toList());
		Collections.sort(list);
		return list;
	}, (list) -> list.stream().collect(Collectors.toMap((size) -> String.valueOf(size.getIndex()), Integer2DynamicParticleSize::getObject)));

	public static final Codec<DynamicSizesWithInterpolation> CODEC = create((instance) -> instance.group(
			option("sizes", new ArrayList<>(), SIZES_CODEC, DynamicSizesWithInterpolation::getSizes),
			option("interpolation", DynamicSizeInterpolation.LINEAR_INTERPOLATION, DynamicSizeInterpolation.CODEC, DynamicSizesWithInterpolation::getInterpolation)
	).apply(instance, DynamicSizesWithInterpolation::new));


}

