package net.lopymine.ip.resourcepack.manager;

import com.google.gson.*;
import com.mojang.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;

public abstract class AbstractConfigsManager<C> {

	public static final String CONFIG_EXTENSION = ".json";
	public static final String LEGACY_CONFIG_EXTENSION = ".json5";

	private final Map<Identifier, String> sourcePacks = new HashMap<>();

	protected abstract String getFolderName();

	protected abstract String getLegacyFolderName();

	protected abstract Codec<C> getCodec();

	protected abstract String getModId();

	protected abstract String getConfigName();

	protected abstract MossyLogger getLogger();

	protected abstract void registerConfig(C config, Identifier id);

	public String getFolderPath() {
		return "%s/%s".formatted(this.getModId(), this.getFolderName());
	}

	public Map<Identifier, String> getSourcePacks() {
		return Collections.unmodifiableMap(this.sourcePacks);
	}

	public void reload() {
		this.getLogger().info("Started registration {} from resources...", this.getConfigName().toUpperCase(Locale.ROOT));
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		this.sourcePacks.clear();
		Map<Identifier, Resource> resources = new LinkedHashMap<>();
		Set<String> legacyLayoutPacks = new LinkedHashSet<>();
		Set<String> legacyExtensionPacks = new LinkedHashSet<>();

		resourceManager.listResources(this.getLegacyFolderName(), (id) -> isConfig(id) && id.getNamespace().equals(this.getModId())).forEach((id, resource) -> {
			resources.put(this.normalizeLegacyId(id), resource);
			legacyLayoutPacks.add(resource.sourcePackId());
		});

		resourceManager.listResources(this.getFolderPath(), AbstractConfigsManager::isConfig)
				.forEach((id, resource) -> resources.put(normalizeId(id), resource));

		resourceManager.listResources(this.getLegacyFolderName(), (id) -> isLegacyExtensionConfig(id) && id.getNamespace().equals(this.getModId()))
				.forEach((id, resource) -> legacyExtensionPacks.add(resource.sourcePackId()));

		resourceManager.listResources(this.getFolderPath(), AbstractConfigsManager::isLegacyExtensionConfig)
				.forEach((id, resource) -> legacyExtensionPacks.add(resource.sourcePackId()));

		this.warnAboutLegacyLayout(legacyLayoutPacks);
		this.warnAboutLegacyExtension(legacyExtensionPacks);

		List<LoadedConfig<C>> configs = new ArrayList<>();

		resources.forEach((id, resource) -> {
			try (InputStream inputStream = resource.open();
			     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				JsonElement json = JsonParser.parseReader(reader);
				C config = this.getCodec().decode(JsonOps.INSTANCE, json)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, this.getLogger()::error)*//*?}*/.getFirst();
				configs.add(new LoadedConfig<>(id, config, getKeys(json), resource.sourcePackId()));
			} catch (Exception e) {
				this.getLogger().error("Failed to parse {} from \"{}\"! Reason:", this.getConfigName(), id, e);
			}
		});

		int registeredConfigs = this.registerConfigs(configs);
		this.getLogger().info("Registration finished, found: {}, registered: {}", configs.size(), registeredConfigs);
	}

	protected int registerConfigs(List<LoadedConfig<C>> configs) {
		int registeredConfigs = 0;
		for (LoadedConfig<C> config : configs) {
			this.register(config);
			registeredConfigs++;
		}
		return registeredConfigs;
	}

	protected void register(LoadedConfig<C> config) {
		this.registerConfig(config.config(), config.id());
		this.sourcePacks.put(config.id(), config.sourcePackId());
		this.getLogger().debug("Registered {} at \"{}\"", this.getConfigName(), config.id());
	}

	private void warnAboutLegacyLayout(Set<String> packs) {
		if (packs.isEmpty()) {
			return;
		}
		this.getLogger().warn(
				"Found {} in the legacy assets layout (\"{}\"), it will be removed in the next major version. Move them to \"{}\". Packs: {}",
				this.getConfigName(),
				this.getLegacyFolderName(),
				this.getFolderPath(),
				String.join(", ", packs)
		);
	}

	private void warnAboutLegacyExtension(Set<String> packs) {
		if (packs.isEmpty()) {
			return;
		}
		this.getLogger().warn(
				"Found {} with the \"{}\" extension, this format is not supported anymore, these files are ignored. Rename them to \"{}\". Packs: {}",
				this.getConfigName(),
				LEGACY_CONFIG_EXTENSION,
				CONFIG_EXTENSION,
				String.join(", ", packs)
		);
	}

	private Identifier normalizeLegacyId(Identifier id) {
		String path = id.getPath().substring(this.getLegacyFolderName().length() + 1);
		return InventoryParticles.id("%s/%s".formatted(this.getFolderPath(), removeExtension(path)));
	}

	private static Identifier normalizeId(Identifier id) {
		return id.withPath(removeExtension(id.getPath()));
	}

	private static String removeExtension(String path) {
		int index = path.lastIndexOf('.');
		return index == -1 ? path : path.substring(0, index);
	}

	private static Set<String> getKeys(JsonElement json) {
		return json instanceof JsonObject object ? new HashSet<>(object.keySet()) : Set.of();
	}

	private static boolean isConfig(Identifier id) {
		return id.getPath().endsWith(CONFIG_EXTENSION);
	}

	private static boolean isLegacyExtensionConfig(Identifier id) {
		return id.getPath().endsWith(LEGACY_CONFIG_EXTENSION);
	}

	public record LoadedConfig<C>(Identifier id, C config, Set<String> keys, String sourcePackId) { }
}

