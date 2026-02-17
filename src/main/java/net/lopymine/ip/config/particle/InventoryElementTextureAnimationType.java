package net.lopymine.ip.config.particle;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.*;

public enum InventoryElementTextureAnimationType implements StringRepresentable {

	STRETCH,
	ONETIME,
	LOOP,
	RANDOM,
	RANDOM_STATIC;

	public static final Codec<InventoryElementTextureAnimationType> CODEC = StringRepresentable.fromEnum(InventoryElementTextureAnimationType::values);

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
