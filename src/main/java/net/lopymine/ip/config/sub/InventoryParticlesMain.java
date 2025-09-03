package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesMain {

	public static final Codec<InventoryParticlesMain> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			option("mod_enabled", true, Codec.BOOL, InventoryParticlesMain::isModEnabled),
			option("debug_mode_enabled", false, Codec.BOOL, InventoryParticlesMain::isDebugModeEnabled),
			option("nbt_debug_mode_enabled", false, Codec.BOOL, InventoryParticlesMain::isNbtDebugModeEnabled)
	).apply(inst, InventoryParticlesMain::new));

	private boolean modEnabled;
	private boolean debugModeEnabled;
	private boolean nbtDebugModeEnabled;

	public static Supplier<InventoryParticlesMain> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
