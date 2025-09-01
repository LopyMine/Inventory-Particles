package net.lopymine.ip.config;

import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.optimization.ParticleDeletionMode;
import net.lopymine.ip.config.spawn.ParticleSpawnType;
import org.slf4j.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.lopymine.ip.utils.*;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesConfig {

	public static final Codec<InventoryParticlesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("mod_enabled", true, Codec.BOOL, InventoryParticlesConfig::isModEnabled),
			option("debug_mode_enabled", false, Codec.BOOL, InventoryParticlesConfig::isDebugModeEnabled),
			option("nbt_debug_mode_enabled", false, Codec.BOOL, InventoryParticlesConfig::isNbtDebugModeEnabled),
			option("particle_spawn_type", ParticleSpawnType.ONLY_IN_CURSOR, ParticleSpawnType.CODEC, InventoryParticlesConfig::getParticleSpawnType),
			option("global_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getGlobalParticlesSpawnCountCoefficient),
			option("global_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getGlobalParticlesSpawnRangeCoefficient),
			option("cursor_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getCursorParticlesSpawnCountCoefficient),
			option("cursor_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getCursorParticlesSpawnRangeCoefficient),
			option("gui_particles_spawn_count_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getGuiParticlesSpawnCountCoefficient),
			option("gui_particles_spawn_range_coefficient", 1.0F, Codec.FLOAT, InventoryParticlesConfig::getGuiParticlesSpawnRangeCoefficient),
			option("max_particles", 5000, Codec.INT, InventoryParticlesConfig::getMaxParticles),
			option("particle_deletion_type", ParticleDeletionMode.OLDEST, ParticleDeletionMode.CODEC, InventoryParticlesConfig::getParticleDeletionMode)
	).apply(instance, InventoryParticlesConfig::new));

	private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(InventoryParticles.MOD_ID + ".json5").toFile();
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Config");
	private static InventoryParticlesConfig INSTANCE;

	private boolean modEnabled;
	private boolean debugModeEnabled;
	private boolean nbtDebugModeEnabled;
	private ParticleSpawnType particleSpawnType;
	private float globalParticlesSpawnCountCoefficient;
	private float globalParticlesSpawnRangeCoefficient;
	private float cursorParticlesSpawnCountCoefficient;
	private float cursorParticlesSpawnRangeCoefficient;
	private float guiParticlesSpawnCountCoefficient;
	private float guiParticlesSpawnRangeCoefficient;
	private int maxParticles;
	private ParticleDeletionMode particleDeletionMode;

	private InventoryParticlesConfig() {
		throw new IllegalArgumentException();
	}

	public static InventoryParticlesConfig getInstance() {
		return INSTANCE == null ? reload() : INSTANCE;
	}

	public static InventoryParticlesConfig reload() {
		return INSTANCE = read();
	}

	public static InventoryParticlesConfig getNewInstance() {
		return CodecUtils.parseNewInstanceHacky(CODEC);
	}

	private static InventoryParticlesConfig read() {
		return ConfigUtils.readConfig(CODEC, CONFIG_FILE, LOGGER);
	}

	public void saveAsync() {
		CompletableFuture.runAsync(this::save);
	}

	public void save() {
		ConfigUtils.saveConfig(this, CODEC, CONFIG_FILE, LOGGER);
	}
}
