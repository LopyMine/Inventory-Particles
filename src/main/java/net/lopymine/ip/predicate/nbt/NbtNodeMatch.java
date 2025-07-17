package net.lopymine.ip.predicate.nbt;

import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.util.StringIdentifiable;

@Getter
public enum NbtNodeMatch implements StringIdentifiable {

	ALL,
	ANY,
	NONE;

	public static final Codec<NbtNodeMatch> CODEC = StringIdentifiable.createCodec(NbtNodeMatch::values);

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
