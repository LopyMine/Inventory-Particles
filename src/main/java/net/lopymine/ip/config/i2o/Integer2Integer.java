package net.lopymine.ip.config.i2o;

import com.mojang.serialization.Codec;
import lombok.*;

@Getter
public class Integer2Integer extends Integer2Object<Integer> {

	public static final Codec<Integer2Integer> CODEC = Integer2Object.getCodec(
			"multiplier",
			1,
			Codec.INT,
			Integer2Integer::new
	);

	public Integer2Integer(int index, Integer object) {
		super(index, object);
	}
}
