package net.lopymine.ip.config.optimization;

import com.mojang.serialization.Codec;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.yacl.utils.EnumWithText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ParticleDeletionMode implements StringIdentifiable, EnumWithText {

	RANDOM,
	OLDEST;

	public static final Codec<ParticleDeletionMode> CODEC = StringIdentifiable.createCodec(ParticleDeletionMode::values);

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}

	@Override
	public Text getText() {
		return InventoryParticles.text("modmenu.option.particle_deletion_mode." + this.asString());
	}
}
