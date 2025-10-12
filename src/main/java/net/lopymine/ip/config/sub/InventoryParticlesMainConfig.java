package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesMainConfig {

	public static final Codec<InventoryParticlesMainConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			option("mod_enabled", true, Codec.BOOL, InventoryParticlesMainConfig::isModEnabled),
			option("debug_mode_enabled", false, Codec.BOOL, InventoryParticlesMainConfig::isDebugModeEnabled),
			option("nbt_debug_mode_enabled", false, Codec.BOOL, InventoryParticlesMainConfig::isNbtDebugModeEnabled)
	).apply(inst, InventoryParticlesMainConfig::new));

	private boolean modEnabled;
	private boolean debugModeEnabled;
	private boolean nbtDebugModeEnabled;

	public static Supplier<InventoryParticlesMainConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
