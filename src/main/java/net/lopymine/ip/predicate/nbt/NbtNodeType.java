package net.lopymine.ip.predicate.nbt;

import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.util.StringIdentifiable;

@Getter
public enum NbtNodeType implements StringIdentifiable {

	OBJECT(10),
	STRING(8),
	LIST(9),
	INT(3);

	public static final Codec<NbtNodeType> CODEC = StringIdentifiable.createCodec(NbtNodeType::values);

	private final int id;

	NbtNodeType(int id) {
		this.id = id;
	}

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
