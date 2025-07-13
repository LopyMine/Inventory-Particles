package net.lopymine.ip.manager;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.element.*;
import net.lopymine.ip.spawner.ParticleSpawner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.*;

public class InvParticleConfigManager {

	private static final Map<Item, List<ParticleSpawner>> ITEM_PARTICLE_CONFIGS = new HashMap<>();

	public static void register() {
		ITEM_PARTICLE_CONFIGS.clear();

		InventoryParticlesClient.LOGGER.info("Started registering particle configs from resources...");
		ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();
		resourceManager.streamResourcePacks().forEach((pack) -> {
			pack.findResources(ResourceType.CLIENT_RESOURCES, InventoryParticles.MOD_ID, "particles", (id, suppler) -> {
				if (!id.getPath().endsWith(".json5") && !id.getPath().endsWith("json")) {
					return;
				}
				foundConfigs.getAndIncrement();
				try (InputStream inputStream = suppler.get(); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
					InvParticleConfig config = InvParticleConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, LOGGER::error)*//*?}*/.getFirst();
					for (InvParticleHolder holder : config.getHolders()) {
						Item item = holder.getItem();
						List<ParticleSpawner> list = ITEM_PARTICLE_CONFIGS.get(item);
						ParticleSpawner itemParticleSpawner = holder.create(config::createParticle);
						if (list == null) {
							List<ParticleSpawner> configs = new ArrayList<>();
							configs.add(itemParticleSpawner);
							ITEM_PARTICLE_CONFIGS.put(item, configs);
						} else {
							list.add(itemParticleSpawner);
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

	public static Map<Item, List<ParticleSpawner>> getItemParticleConfigs() {
		return ITEM_PARTICLE_CONFIGS;
	}
}
