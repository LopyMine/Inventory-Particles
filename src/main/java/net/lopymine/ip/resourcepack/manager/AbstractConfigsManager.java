package net.lopymine.ip.resourcepack.manager;

import com.google.gson.JsonParser;
import com.mojang.serialization.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

public abstract class AbstractConfigsManager<C> {

	protected abstract String getFolderName();

	protected abstract Codec<C> getCodec();

	protected abstract String getConfigName();

	protected abstract MossyLogger getLogger();

	protected abstract void registerConfig(C config);

	public void reload() {
		this.getLogger().info("Started registration {} from resources...", this.getConfigName().toUpperCase(Locale.ROOT));
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		AtomicInteger foundConfigs = new AtomicInteger();
		AtomicInteger registeredConfigs = new AtomicInteger();

		resourceManager.listResources(this.getFolderName(), (id) -> id.getPath().endsWith(".json5") || id.getPath().endsWith(".json"))
				.forEach((id, resource) -> {
					foundConfigs.getAndIncrement();

					try (InputStream inputStream = resource.open();
						 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
						C config = this.getCodec().decode(JsonOps.INSTANCE, JsonParser.parseReader(reader))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, this.getLogger()::error)*//*?}*/.getFirst();
						this.registerConfig(config);
						this.getLogger().debug("Registered {} at \"{}\"", this.getConfigName(), id);
						registeredConfigs.getAndIncrement();
					} catch (Exception e) {
						this.getLogger().error("Failed to parse {} from \"{}\"! Reason:", this.getConfigName(), id, e);
					}
				});

		this.getLogger().info("Registration finished, found: {}, registered: {}", foundConfigs.get(), registeredConfigs.get());
	}
}

