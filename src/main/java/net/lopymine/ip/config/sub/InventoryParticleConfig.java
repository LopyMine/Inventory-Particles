package net.lopymine.ip.config.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import lombok.*;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.spawn.ParticleSpawnType;
import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticleConfig {

	public static final Codec<InventoryParticleConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			option("particle_spawn_type", ParticleSpawnType.IN_CURSOR, ParticleSpawnType.CODEC, InventoryParticleConfig::getParticleSpawnType),
			option("particle_deletion_type", ParticleDeletionMode.OLDEST, ParticleDeletionMode.CODEC, InventoryParticleConfig::getParticleDeletionMode),
			option("max_particles", 5000, Codec.INT, InventoryParticleConfig::getMaxParticles)
	).apply(inst, InventoryParticleConfig::new));

	private ParticleSpawnType particleSpawnType;
	private ParticleDeletionMode particleDeletionMode;
	private int maxParticles;

	public static Supplier<InventoryParticleConfig> getNewInstance() {
		return () -> CodecUtils.parseNewInstanceHacky(CODEC);
	}
}
