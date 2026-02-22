package net.lopymine.ip.config.optimization;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.yacl.utils.EnumWithText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ParticlesDeletionMode implements StringRepresentable, EnumWithText {

	RANDOM,
	OLDEST;

	public static final Codec<ParticlesDeletionMode> CODEC = StringRepresentable.fromEnum(ParticlesDeletionMode::values);

	@Override
	public @NotNull String getSerializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	@Override
	public Component getText() {
		return InventoryParticles.text("modmenu.option.particles_deletion_mode." + this.getSerializedName());
	}
}
