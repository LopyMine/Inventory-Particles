package net.lopymine.ip.resourcepack.manager;

import com.google.gson.JsonParser;
import com.mojang.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;

public abstract class AbstractConfigsManager<C> {

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
		Map<Identifier, Resource> configs = new LinkedHashMap<>();
		Set<String> legacyPacks = new LinkedHashSet<>();

		resourceManager.listResources(this.getLegacyFolderName(), (id) -> isConfigPath(id) && id.getNamespace().equals(this.getModId()))
				.forEach((id, resource) -> {
					configs.put(this.normalizeLegacyId(id), resource);
					legacyPacks.add(resource.sourcePackId());
				});

		resourceManager.listResources(this.getFolderPath(), AbstractConfigsManager::isConfigPath)
				.forEach((id, resource) -> configs.put(normalizeId(id), resource));

		if (!legacyPacks.isEmpty()) {
			this.getLogger().warn(
					"Found {} in the legacy assets layout (\"{}\"), it will be removed in the next major version. Move them to \"{}\". Packs: {}",
					this.getConfigName(), this.getLegacyFolderName(), this.getFolderPath(), String.join(", ", legacyPacks)
			);
		}

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		configs.forEach((id, resource) -> {
			foundConfigs.getAndIncrement();

			try (InputStream inputStream = resource.open();
			     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				C config = this.getCodec().decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, this.getLogger()::error)*//*?}*/.getFirst();
				this.registerConfig(config, id);
				this.sourcePacks.put(id, resource.sourcePackId());
				this.getLogger().debug("Registered {} at \"{}\"", this.getConfigName(), id);
				registeredConfigs.getAndIncrement();
			} catch (Exception e) {
				this.getLogger().error("Failed to parse {} from \"{}\"! Reason:", this.getConfigName(), id, e);
			}
		});

		this.getLogger().info("Registration finished, found: {}, registered: {}", foundConfigs.get(), registeredConfigs.get());
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

	private static boolean isConfigPath(Identifier id) {
		return id.getPath().endsWith(".json5") || id.getPath().endsWith(".json");
	}
}

