package net.lopymine.ip.resourcepack;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.spawner.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.*;

public class ResourcePackParticleConfigsManager {

	private static final Map<Item, List<IParticleSpawner>> PER_ITEM_PARTICLE_SPAWNERS = new HashMap<>();

	public static void reload() {
		PER_ITEM_PARTICLE_SPAWNERS.clear();

		InventoryParticlesClient.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();
		resourceManager.streamResourcePacks().forEach((pack) -> {
			pack.findResources(ResourceType.CLIENT_RESOURCES, InventoryParticles.MOD_ID, InventoryParticlesAtlasManager.FOLDER_ID.getPath(), (id, suppler) -> {
				if (!id.getPath().endsWith(".json5") && !id.getPath().endsWith("json")) {
					return;
				}
				foundConfigs.getAndIncrement();
				try (InputStream inputStream = suppler.get(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
					ParticleConfig config = ParticleConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, LOGGER::error)*//*?}*/.getFirst();
					for (ParticleHolder holder : config.getHolders()) {
						Item item = holder.getItem().getItem();
						List<IParticleSpawner> list = PER_ITEM_PARTICLE_SPAWNERS.get(item);
						ParticleSpawner spawner = holder.create(config::createParticle);
						if (list == null) {
							List<IParticleSpawner> spawners = new ArrayList<>();
							spawners.add(spawner);
							PER_ITEM_PARTICLE_SPAWNERS.put(item, spawners);
						} else {
							list.add(spawner);
						}
					}
					InventoryParticlesClient.LOGGER.debug("Registered config at \"{}\"", id);
					registeredConfigs.getAndIncrement();
				} catch (Exception e) {
					InventoryParticlesClient.LOGGER.error("Failed to parse particle config from \"{}\"! Reason:", id, e);
				}
			});
		});
		InventoryParticlesClient.LOGGER.info("Registering finished, found: {}, registered: {}", foundConfigs.get(), registeredConfigs.get());
	}

	public static Map<Item, List<IParticleSpawner>> getPerItemParticleSpawners() {
		return PER_ITEM_PARTICLE_SPAWNERS;
	}
}
