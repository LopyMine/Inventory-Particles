package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;

import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticleConfig {

	public static final Codec<InventoryParticleConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			option("cursor_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isCursorSpawnEnabled),
			option("hovered_slot_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isHoveredSlotSpawnEnabled),
			option("all_slots_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isGuiSlotsSpawnEnabled),
			option("gui_action_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isGuiActionSpawnEnabled),
			option("gui_action_take_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isGuiActionTakeSpawnEnabled),
			option("gui_action_put_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isGuiActionPutSpawnEnabled),
			option("particle_deletion_type", ParticleDeletionMode.OLDEST, ParticleDeletionMode.CODEC, InventoryParticleConfig::getParticleDeletionMode),
			option("max_particles", 5000, Codec.INT, InventoryParticleConfig::getMaxParticles),
			option("particle_transparency", 1.0F, Codec.FLOAT, InventoryParticleConfig::getParticleTransparency)
	).apply(inst, InventoryParticleConfig::new));

	private boolean cursorSpawnEnabled;
	private boolean hoveredSlotSpawnEnabled;
	private boolean guiSlotsSpawnEnabled;
	private boolean guiActionSpawnEnabled;
	private boolean guiActionTakeSpawnEnabled;
	private boolean guiActionPutSpawnEnabled;
	private ParticleDeletionMode particleDeletionMode;
	private int maxParticles;
	private float particleTransparency;

	public static Supplier<InventoryParticleConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
