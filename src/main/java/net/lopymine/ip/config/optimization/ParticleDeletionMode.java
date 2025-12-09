package net.lopymine.ip.config.optimization;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.yacl.utils.EnumWithText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum ParticleDeletionMode implements StringRepresentable, EnumWithText {

	RANDOM,
	OLDEST;

	public static final Codec<ParticleDeletionMode> CODEC = StringRepresentable.fromEnum(ParticleDeletionMode::values);

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public Component getText() {
		return InventoryParticles.text("modmenu.option.particle_deletion_mode." + this.getSerializedName());
	}
}
