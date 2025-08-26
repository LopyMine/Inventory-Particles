package net.lopymine.ip.config.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@AllArgsConstructor
public abstract class Integer2Object<O> {

	private final int index;
	private final O object;

	public static <A, C extends Integer2Object<A>> Codec<C> getCodec(String resultName, A standardValue, Codec<A> codec, Factory<C, A> factory) {
		return RecordCodecBuilder.create((instance) -> instance.group(
				option("from", 0, Codec.INT, Integer2Object::getIndex),
				option(resultName, standardValue, codec, Integer2Object::getObject)
		).apply(instance, factory::create));
	}

	public interface Factory<A, O> {

		A create(int index, O o);

	}

}
