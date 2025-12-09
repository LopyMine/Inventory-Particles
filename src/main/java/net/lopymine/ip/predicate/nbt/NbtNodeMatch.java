package net.lopymine.ip.predicate.nbt;

import com.mojang.serialization.Codec;
import java.util.Locale;
import lombok.Getter;
import net.minecraft.util.StringRepresentable;

@Getter
public enum NbtNodeMatch implements StringRepresentable {

	ALL,
	ANY,
	NONE;

	public static final Codec<NbtNodeMatch> CODEC = StringRepresentable.fromEnum(NbtNodeMatch::values);

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
