package net.lopymine.ip.config;

import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.sub.*;
import net.lopymine.mossylib.utils.*;
import org.slf4j.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;

import net.lopymine.ip.utils.*;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesConfig {

	public static final Codec<InventoryParticlesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("main", InventoryParticlesMainConfig.getNewInstance(), InventoryParticlesMainConfig.CODEC, InventoryParticlesConfig::getMainConfig),
			option("particle", InventoryParticleConfig.getNewInstance(), InventoryParticleConfig.CODEC, InventoryParticlesConfig::getParticleConfig),
			option("coefficients", InventoryParticlesCoefficientsConfig.getNewInstance(), InventoryParticlesCoefficientsConfig.CODEC, InventoryParticlesConfig::getCoefficientsConfig)
	).apply(instance, InventoryParticlesConfig::new));

	private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(InventoryParticles.MOD_ID + ".json5").toFile();
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Config");
	private static InventoryParticlesConfig INSTANCE;

	private InventoryParticlesMainConfig mainConfig;
	private InventoryParticleConfig particleConfig;
	private InventoryParticlesCoefficientsConfig coefficientsConfig;

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
