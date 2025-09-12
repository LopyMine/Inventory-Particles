package net.lopymine.ip.resourcepack;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.spawner.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ResourcePackParticleConfigsManager {

	private static final Map<Item, List<IParticleSpawner>> PER_ITEM_PARTICLE_SPAWNERS = new HashMap<>();

	public static void reload() {
		PER_ITEM_PARTICLE_SPAWNERS.clear();

		InventoryParticlesClient.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		resourceManager.findResources(InventoryParticlesAtlasManager.FOLDER_ID.getPath(), (id) -> id.getPath().endsWith(".json5") || id.getPath().endsWith("json")).forEach((id, resource) -> {
			foundConfigs.getAndIncrement();
			try (InputStream inputStream = resource.getInputStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				ParticleConfig config = ParticleConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
				for (ParticleHolder holder : config.getHolders()) {
					Item item = holder.getItem().getItem();
					ParticleSpawner spawner = holder.create(config::createParticle);
					registerItemSpawner(item, spawner);
				}
				InventoryParticlesClient.LOGGER.debug("Registered config at \"{}\"", id);
				registeredConfigs.getAndIncrement();
			} catch (Exception e) {
				InventoryParticlesClient.LOGGER.error("Failed to parse particle config from \"{}\"! Reason:", id, e);
			}

		});

		InventoryParticlesClient.LOGGER.info("Registering finished, found: {}, registered: {}", foundConfigs.get(), registeredConfigs.get());
	}

	public static void registerItemSpawner(Item item, IParticleSpawner spawner) {
		PER_ITEM_PARTICLE_SPAWNERS.computeIfAbsent(item, (i) -> new ArrayList<>()).add(spawner);
	}

	public static Map<Item, List<IParticleSpawner>> getPerItemParticleSpawners() {
		return PER_ITEM_PARTICLE_SPAWNERS;
	}
}
