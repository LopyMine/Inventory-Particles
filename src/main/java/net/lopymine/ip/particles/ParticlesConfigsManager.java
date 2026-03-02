package net.lopymine.ip.particles;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.spawner.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ParticlesConfigsManager {

	private static final Map<ParticleHolder, RegisteredConfig> REGISTERED_CONFIGS = new HashMap<>();
	private static final Map<Item, List<IParticleSpawner>> PER_ITEM_PARTICLE_SPAWNERS = new IdentityHashMap<>();
	private static final Map<TagKey<Item>, List<IParticleSpawner>> PER_TAG_PARTICLE_SPAWNERS = new HashMap<>();

	private static final Map<Item, List<IParticleSpawner>> COMBINED_MAP = new IdentityHashMap<>();

	public static void reload() {
		PER_ITEM_PARTICLE_SPAWNERS.clear();
		PER_TAG_PARTICLE_SPAWNERS.clear();

		InventoryParticlesClient.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		resourceManager.listResources(InventoryParticlesAtlasManager.FOLDER_ID.getPath(), (id) -> id.getPath().endsWith(".json5") || id.getPath().endsWith("json")).forEach((id, resource) -> {
			foundConfigs.getAndIncrement();
			try (InputStream inputStream = resource.open(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				ParticleConfig config = ParticleConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
				for (ParticleHolder holder : config.getHolders()) {
					ParticleSpawner spawner = holder.create(config::createParticle);
					Either<CachedItem, Identifier> itemOrTag = holder.getItemOrTag();
					itemOrTag.ifLeft((cachedItem) -> {
						Item item = cachedItem.getItem();
						registerItemSpawner(id, item, holder, spawner);
					});
					itemOrTag.ifRight((tag) -> {
						TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag);
						registerItemSpawner(id, tagKey, holder, spawner);
					});
				}
				InventoryParticlesClient.LOGGER.debug("Registered config at \"{}\"", id);
				registeredConfigs.getAndIncrement();
			} catch (Exception e) {
				InventoryParticlesClient.LOGGER.error("Failed to parse particle config from \"{}\"! Reason:", id, e);
			}

		});

		if (Minecraft.getInstance().level != null) {
			updateCombinedMap();
		}

		InventoryParticlesClient.LOGGER.info("Registering finished, found: {}, registered: {}", foundConfigs.get(), registeredConfigs.get());
	}

	public static void registerItemSpawner(Identifier location, Item item, ParticleHolder holder, IParticleSpawner spawner) {
		PER_ITEM_PARTICLE_SPAWNERS.computeIfAbsent(item, (i) -> new ArrayList<>()).add(spawner);
		REGISTERED_CONFIGS.put(holder, new RegisteredConfig(location, spawner));
	}

	public static void registerItemSpawner(Identifier location, TagKey<Item> item, ParticleHolder holder, IParticleSpawner spawner) {
		PER_TAG_PARTICLE_SPAWNERS.computeIfAbsent(item, (i) -> new ArrayList<>()).add(spawner);
		REGISTERED_CONFIGS.put(holder, new RegisteredConfig(location, spawner));
	}

	@SuppressWarnings("deprecation")
	public static void updateCombinedMap() {
		InventoryParticlesConfig.getInstance().getWhitelistsConfig().recompileAll();

		COMBINED_MAP.clear();

		for (Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
			Identifier identifier = entry.getKey().identifier();
			Item item = entry.getValue();

			List<IParticleSpawner> spawners = new ArrayList<>();

			List<IParticleSpawner> itemSpecificSpawners = PER_ITEM_PARTICLE_SPAWNERS.get(item);
			if (itemSpecificSpawners != null) {
				spawners.addAll(itemSpecificSpawners);
			}

			if (!identifier.getNamespace().equals("minecraft")) {
				spawners.addAll(item.builtInRegistryHolder()
						.tags()
						.map(PER_TAG_PARTICLE_SPAWNERS::get)
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.toList());
			}

			if (spawners.isEmpty()) {
				continue;
			}
			COMBINED_MAP.put(item, spawners);
		}
	}

	public static Map<ParticleHolder, RegisteredConfig> getRegisteredConfigs() {
		return REGISTERED_CONFIGS;
	}

	@Nullable
	public static List<IParticleSpawner> getSpawnersForItem(Item item) {
		return COMBINED_MAP.get(item);
	}

	public record RegisteredConfig(Identifier id, IParticleSpawner spawner) {}

}
