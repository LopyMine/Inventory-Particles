package net.lopymine.ip.config.spawn;

import com.mojang.serialization.Codec;
import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.yacl.utils.EnumWithText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

@Getter
public enum ParticleSpawnType implements EnumWithText, StringIdentifiable {

	EVERYWHERE(true, true, true),
	SLOTS(false, true, false),
	HOVERED_SLOT(true, false, false),
	IN_CURSOR(false, false, true),
	HOVERED_SLOT_AND_IN_CURSOR(true, false, true),
	SLOTS_AND_IN_CURSOR(false, true, true);

	public static final Codec<ParticleSpawnType> CODEC = StringIdentifiable.createCodec(ParticleSpawnType::values);

	private final boolean hoveredSlot;
	private final boolean slots;
	private final boolean cursor;

	ParticleSpawnType(boolean hoveredSlot, boolean slots, boolean cursor) {
		this.hoveredSlot = hoveredSlot;
		this.slots       = slots;
		this.cursor      = cursor;
	}

	@Override
	public Text getText() {
		return InventoryParticles.text("modmenu.option.particle_spawn_type." + this.asString());
	}

	@Override
	public String asString() {
		return this.name().toLowerCase();
	}
}
