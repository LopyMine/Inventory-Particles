package net.lopymine.ip.config;

import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.sub.*;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.utils.*;
import org.slf4j.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class InventoryParticlesConfig {

	public static final Codec<InventoryParticlesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			option("test_2_1_0", false, Codec.BOOL, InventoryParticlesConfig::isTest210),
			option("test_1_2_2", false, Codec.BOOL, InventoryParticlesConfig::isTest122),
			option("main", InventoryParticlesMainConfig.getNewInstance(), InventoryParticlesMainConfig.CODEC, InventoryParticlesConfig::getMainConfig),
			option("particle", InventoryParticleConfig.getNewInstance(), InventoryParticleConfig.CODEC, InventoryParticlesConfig::getParticleConfig),
			option("coefficients", InventoryParticlesCoefficientsConfig.getNewInstance(), InventoryParticlesCoefficientsConfig.CODEC, InventoryParticlesConfig::getCoefficientsConfig),
			option("whitelists", InventoryParticlesItemWhitelistsConfig.getNewInstance(), InventoryParticlesItemWhitelistsConfig.CODEC, InventoryParticlesConfig::getWhitelistsConfig),
			option("cache", InventoryParticlesCacheConfig.getNewInstance(), InventoryParticlesCacheConfig.CODEC, InventoryParticlesConfig::getCacheConfig)
	).apply(instance, InventoryParticlesConfig::new));

	private static final File CONFIG_FILE = MossyLoader.getConfigDir().resolve(InventoryParticles.MOD_ID + ".json5").toFile();
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Config");
	private static InventoryParticlesConfig INSTANCE;

	private boolean test210;
	private boolean test122;
	private InventoryParticlesMainConfig mainConfig;
	private InventoryParticleConfig particleConfig;
	private InventoryParticlesCoefficientsConfig coefficientsConfig;
	private InventoryParticlesItemWhitelistsConfig whitelistsConfig;
	private InventoryParticlesCacheConfig cacheConfig;

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
		InventoryParticlesConfig config = ConfigUtils.readConfig(CODEC, CONFIG_FILE, LOGGER);
		if (!config.isTest122()) {
			config.setTest122(true);
			InventoryParticleConfig particleConfig = config.getParticleConfig();
			particleConfig.setGuiActionsSpawnEnabled(true);
			particleConfig.setGuiActionPutSpawnEnabled(true);
			particleConfig.setGuiActionTakeSpawnEnabled(true);
			particleConfig.setGuiActionQuickMoveSpawnEnabled(true);
			config.getCoefficientsConfig().getGuiActionConfig().setCooldownCoefficient(100);
			config.saveAsync();
		}
		return config;
	}

	public void saveAsync() {
		CompletableFuture.runAsync(this::save);
	}

	public void save() {
		ConfigUtils.saveConfig(this, CODEC, CONFIG_FILE, LOGGER);
	}
}
