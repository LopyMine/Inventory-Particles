package net.lopymine.ip.config.misc;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.config.range.IntegerRange;
import net.minecraft.util.Identifier;
import static com.mojang.serialization.Codec.*;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.ip.utils.CodecUtils.option;
import static net.minecraft.util.Identifier.CODEC;

@Getter
@AllArgsConstructor
public class IntegerRange2Int {

	private String test;
	private int index;
	private boolean blablabla;
	private Identifier idToSomething;

	private record Key(IntegerRange range, int i) {


	}

}
