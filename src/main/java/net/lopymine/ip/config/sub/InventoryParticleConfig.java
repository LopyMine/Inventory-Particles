package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.config.optimization.ParticlesDeletionMode;

import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticleConfig {

	public static final Codec<InventoryParticleConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			option("cursor_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isCursorSpawnEnabled),
			option("hovered_slot_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isHoveredSlotSpawnEnabled),
			option("all_slots_spawn_enabled", false, Codec.BOOL, InventoryParticleConfig::isGuiSlotsSpawnEnabled),
			option("gui_action_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isGuiActionsSpawnEnabled),
			option("gui_action_take_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isGuiActionTakeSpawnEnabled),
			option("gui_action_put_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isGuiActionPutSpawnEnabled),
			option("gui_action_quick_move_spawn_enabled", true, Codec.BOOL, InventoryParticleConfig::isGuiActionPutSpawnEnabled),
			option("particle_deletion_type", ParticlesDeletionMode.OLDEST, ParticlesDeletionMode.CODEC, InventoryParticleConfig::getParticlesDeletionMode),
			option("max_particles", 5000, Codec.INT, InventoryParticleConfig::getParticlesCountLimit),
			option("particle_transparency", 1.0D, Codec.DOUBLE, InventoryParticleConfig::getParticleTransparency),
			option("fade_out_duration_ticks", 0, Codec.INT, InventoryParticleConfig::getFadeOutDurationTicks)
	).apply(inst, InventoryParticleConfig::new));

	private boolean cursorSpawnEnabled;
	private boolean hoveredSlotSpawnEnabled;
	private boolean guiSlotsSpawnEnabled;
	private boolean guiActionsSpawnEnabled;
	private boolean guiActionTakeSpawnEnabled;
	private boolean guiActionPutSpawnEnabled;
	private boolean guiActionQuickMoveSpawnEnabled;
	private ParticlesDeletionMode particlesDeletionMode;
	private int particlesCountLimit;
	private double particleTransparency;
	private int fadeOutDurationTicks;

	public static Supplier<InventoryParticleConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
