package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import net.lopymine.ip.config.i2o.Integer2DynamicParticleSize;
import net.lopymine.ip.debug.IDebugRenderable;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class DynamicParticleSizes implements IDebugRenderable {

	public static final DynamicParticleSizes STANDARD = new DynamicParticleSizes(List.of(), DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION);

	private List<Integer2DynamicParticleSize> sizes;
	private DynamicParticleSizeInterpolation interpolation;

	public static DynamicParticleSizes fromStatic(StaticParticleSize size) {
		return new DynamicParticleSizes(List.of(Integer2DynamicParticleSize.fromStatic(size)), DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION);
	}

	public static final Codec<List<Integer2DynamicParticleSize>> SIZES_CODEC = Codec.unboundedMap(Codec.STRING, DynamicParticleSize.CODEC).xmap((map) -> {
		List<Integer2DynamicParticleSize> list = new ArrayList<>(map.entrySet().stream().map((entry) -> new Integer2DynamicParticleSize(Integer.parseInt(entry.getKey()), entry.getValue())).toList());
		Collections.sort(list);
		return list;
	}, (list) -> list.stream().collect(Collectors.toMap((size) -> String.valueOf(size.getIndex()), Integer2DynamicParticleSize::getObject)));

	public static final Codec<DynamicParticleSizes> CODEC = create((instance) -> instance.group(
			option("sizes", new ArrayList<>(), SIZES_CODEC, DynamicParticleSizes::getSizes),
			option("interpolation", DynamicParticleSizeInterpolation.LINEAR_INTERPOLATION, DynamicParticleSizeInterpolation.CODEC, DynamicParticleSizes::getInterpolation)
	).apply(instance, DynamicParticleSizes::new));


}

