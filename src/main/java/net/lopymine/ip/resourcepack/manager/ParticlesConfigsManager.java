package net.lopymine.ip.resourcepack.manager;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.Map.Entry;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.spawner.*;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ParticlesConfigsManager extends AbstractConfigsManager<ParticleConfig> {

	public static final Map<Identifier, List<ParticleConfig>> REGISTERED_CONFIGS = new HashMap<>();
	private static final Map<Item, List<IParticleSpawner>> PER_ITEM_PARTICLE_SPAWNERS = new IdentityHashMap<>();
	private static final Map<TagKey<Item>, List<IParticleSpawner>> PER_TAG_PARTICLE_SPAWNERS = new HashMap<>();

	private static final Map<Item, List<IParticleSpawner>> COMBINED_MAP = new IdentityHashMap<>();

	private static final ParticlesConfigsManager INSTANCE = new ParticlesConfigsManager();

	private ParticlesConfigsManager() {}

	public static ParticlesConfigsManager getInstance() {
		return INSTANCE;
	}

	@Override
	protected String getFolderName() {
		return InventoryParticlesAtlasManager.FOLDER_ID.getPath();
	}

	@Override
	protected Codec<ParticleConfig> getCodec() {
		return ParticleConfig.CODEC;
	}

	@Override
	protected String getConfigName() {
		return "particle configs";
	}

	@Override
	protected MossyLogger getLogger() {
		return InventoryParticlesClient.LOGGER;
	}

	@Override
	protected void registerConfig(ParticleConfig config, Identifier id) {
		REGISTERED_CONFIGS.computeIfAbsent(id, (key) -> new ArrayList<>()).add(config);

		for (ParticleHolder holder : config.getHolders()) {
			ParticleSpawner spawner = holder.create(config::createParticle);
			Either<CachedItem, Identifier> itemOrTag = holder.getItemOrTag();
			itemOrTag.ifLeft((cachedItem) -> {
				Item item = cachedItem.getItem();
				registerItemSpawner(item, spawner);
			});
			itemOrTag.ifRight((tag) -> {
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag);
				registerItemSpawner(tagKey, spawner);
			});
		}

		if (Minecraft.getInstance().level != null) {
			updateCombinedMap();
		}
	}

	public void reload() {
		REGISTERED_CONFIGS.clear();
		PER_ITEM_PARTICLE_SPAWNERS.clear();
		PER_TAG_PARTICLE_SPAWNERS.clear();
		super.reload();
	}

	private void registerItemSpawner(Item item, IParticleSpawner spawner) {
		PER_ITEM_PARTICLE_SPAWNERS.computeIfAbsent(item, (i) -> new ArrayList<>()).add(spawner);
	}

	private void registerItemSpawner(TagKey<Item> item, IParticleSpawner spawner) {
		PER_TAG_PARTICLE_SPAWNERS.computeIfAbsent(item, (i) -> new ArrayList<>()).add(spawner);
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

	@Nullable
	public static List<IParticleSpawner> getSpawnersForItem(Item item) {
		return COMBINED_MAP.get(item);
	}

}
