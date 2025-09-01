package net.lopymine.ip.config.spawn;

import com.mojang.serialization.Codec;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.yacl.utils.EnumWithText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ParticleSpawnType implements EnumWithText, StringIdentifiable {

	EVERYWHERE,
	ONLY_IN_CURSOR;

	public static final Codec<ParticleSpawnType> CODEC = StringIdentifiable.createCodec(ParticleSpawnType::values);

	@Override
	public Text getText() {
		return InventoryParticles.text("modmenu.option.particle_spawn_type." + this.asString());
	}

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
