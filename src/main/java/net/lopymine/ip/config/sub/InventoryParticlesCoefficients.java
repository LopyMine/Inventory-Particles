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
public class InventoryParticlesCoefficients {


	public static final Codec<InventoryParticlesCoefficients> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("global_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getGlobalParticlesSpawnCountCoefficient),
			option("global_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getGlobalParticlesSpawnRangeCoefficient),
			option("cursor_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getCursorParticlesSpawnCountCoefficient),
			option("cursor_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getCursorParticlesSpawnRangeCoefficient),
			option("gui_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getGuiParticlesSpawnCountCoefficient),
			option("gui_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesCoefficients::getGuiParticlesSpawnRangeCoefficient)
	).apply(instance, InventoryParticlesCoefficients::new));

	private float globalParticlesSpawnCountCoefficient;
	private float globalParticlesSpawnRangeCoefficient;
	private float cursorParticlesSpawnCountCoefficient;
	private float cursorParticlesSpawnRangeCoefficient;
	private float guiParticlesSpawnCountCoefficient;
	private float guiParticlesSpawnRangeCoefficient;

	public static Supplier<InventoryParticlesCoefficients> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
