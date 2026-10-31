package net.lopymine.ip.family;

import com.mojang.serialization.Codec;
import java.util.*;
import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.resourcepack.manager.AbstractConfigsManager;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Getter
public class FamilyParticlesConfigManager extends AbstractConfigsManager<FamilyParticleConfig> {

	public static final String FOLDER_NAME = "families";
	public static final String LEGACY_FOLDER_NAME = "ifamilies";
	public static final Identifier FALLBACK_CONFIG_ID = InventoryParticles.id("%s/%s/fallback/standard".formatted(InventoryParticles.MOD_ID, FOLDER_NAME));

	private final Map<Identifier, FamilyParticleConfig> registeredConfigsMap = new HashMap<>();
	private final List<FamilyParticleConfig> registeredConfigs = new ArrayList<>();

	private static final FamilyParticlesConfigManager INSTANCE = new FamilyParticlesConfigManager();

	public static FamilyParticlesConfigManager getInstance() {
		return INSTANCE;
	}

	@Override
	protected String getFolderName() {
		return FOLDER_NAME;
	}

	@Override
	protected String getLegacyFolderName() {
		return LEGACY_FOLDER_NAME;
	}

	@Override
	protected Codec<FamilyParticleConfig> getCodec() {
		return FamilyParticleConfig.CODEC;
	}

	@Override
	protected String getConfigName() {
		return "particles family";
	}

	@Override
	protected String getModId() {
		return InventoryParticles.MOD_ID;
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryParticlesClient.LOGGER;
	}

	@Override
	protected void registerConfig(FamilyParticleConfig config, Identifier id) {
		config.setLocation(id);
		this.registeredConfigsMap.computeIfAbsent(id, (key) -> config);
		this.registeredConfigs.add(config);
	}

	@Override
	public void reload() {
		this.registeredConfigsMap.clear();
		this.registeredConfigs.clear();
		super.reload();
		this.getFallbackConfig();
		this.registeredConfigs.sort(Comparator.comparingInt(FamilyParticleConfig::getPriority).reversed());
	}

	@NotNull
	public FamilyParticleConfig getFallbackConfig() {
		FamilyParticleConfig fallbacks = this.registeredConfigsMap.get(FALLBACK_CONFIG_ID);
		if (fallbacks == null) {
			throw new IllegalArgumentException("Failed to find fallback family config for Inventory Particles!");
		}
		return fallbacks;
	}

}
