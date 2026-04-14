package net.lopymine.ip.resourcepack.manager;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.color.StandardColorProvider;
import net.lopymine.ip.element.mod.spawner.*;
import net.lopymine.ip.element.predicate.nbt.NbtNodeMatch;
import net.lopymine.ip.family.*;
import net.lopymine.ip.family.FamilyParticleData.GeneratedTextures;
import net.lopymine.ip.family.generation.ItemRenderingManager;
import net.lopymine.ip.t2o.*;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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
			ParticleSpawner spawner = holder.createSpawner(config::createParticle);
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
	}

	public void reload() {
		REGISTERED_CONFIGS.clear();
		PER_ITEM_PARTICLE_SPAWNERS.clear();
		PER_TAG_PARTICLE_SPAWNERS.clear();
		super.reload();
		if (Minecraft.getInstance().level != null) {
			updateCombinedMap();
		}
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

		Set<Entry<ResourceKey<Item>, Item>> entries = BuiltInRegistries.ITEM.entrySet();
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			int index = 0;
			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				System.out.println("%s / %s".formatted(index, entries.size()));

				Identifier identifier = entry.getKey().identifier();
				Item item = entry.getValue();

				List<IParticleSpawner> spawners = new ArrayList<>();


				List<IParticleSpawner> familyParticles = getFamilyParticles(identifier, item);
				if (familyParticles != null) {
					spawners.addAll(familyParticles);
				}

//			List<IParticleSpawner> specificSpawners = PER_ITEM_PARTICLE_SPAWNERS.get(item);
//			if (specificSpawners != null) {
//				spawners.addAll(specificSpawners);
//			}
//
//			if (!identifier.getNamespace().equals("minecraft") || item instanceof BucketItem) {
//				spawners.addAll(item.builtInRegistryHolder()
//						.tags()
//						.map(PER_TAG_PARTICLE_SPAWNERS::get)
//						.filter(Objects::nonNull)
//						.flatMap(Collection::stream)
//						.toList());
//
//
//				List<IParticleSpawner> familyParticles = getFamilyParticles(identifier, item);
//				if (familyParticles != null) {
//					spawners.addAll(familyParticles);
//				}
//			}

				index++;

				if (spawners.isEmpty()) {
					continue;
				}

				COMBINED_MAP.put(item, spawners);
			}
		}).exceptionally((e) -> {
			e.printStackTrace();
			return null;
		});
	}

	@Nullable
	private static List<IParticleSpawner> getFamilyParticles(Identifier itemId, Item item) {
		List<FamilyParticleConfig> family = FamilyParticlesManager.getFamily(item);
		if (family.isEmpty()) {
			return null;
		}

		FamilyParticleConfig config = family.get(0); // todo add priority
		List<IParticleSpawner> list = new ArrayList<>();

		for (FamilyParticleData particle : config.getParticles()) {
			Identifier id = InventoryParticles.id("%s/%s.json".formatted(getInstance().getFolderName(), particle.getId().getPath()));
			List<ParticleConfig> configs = REGISTERED_CONFIGS.get(id);
			if (configs == null || configs.isEmpty()) {
				getInstance().getLogger().error("Failed to find config from \"%s\" for family config from \"%s\"!".formatted(id.getPath(), config.getLocation()));
				continue;
			}
			for (ParticleConfig particleConfig : configs) {
				ParticleConfig copy = particleConfig.copy();

				RenderedItemImage renderedItemImage = ItemRenderingManager.getRenderedItemImage(item);
				GeneratedTextures generatedTextures = renderedItemImage == null ? null : particle.getGeneratedTextures(renderedItemImage, itemId, item);

				if (generatedTextures != null && copy.getTextures().isEmpty()) {
					copy.setTextures(generatedTextures.textures());
				}

				ParticleSpawnAreaId spawnArea;
				if (renderedItemImage != null) {
					Identifier spawnAreaId = InventoryParticles.id("rii/" + itemId.getPath());
					List<ParticleSpawnPos> pixels = Texture2ObjectsManager.readFromTexture(
							renderedItemImage.getImage(),
							spawnAreaId,
							"family config spawn area",
							() -> (x, y, imageWidth, imageHeight, color) -> {
								if (ArgbUtils2.getAlpha(color) == 0) {
									return false;
								}
								if (generatedTextures == null) {
									return true;
								}
								ArrayList<Integer> colors = generatedTextures.colors();
								if (colors.isEmpty()) {
									return true;
								}
								for (Integer c : colors) {
									int distance = ArgbUtils2.colorDistanceSquared(c, color);
									if (distance <= 60 * 60) {
										return true;
									}
								}
								return false;
							},
							(x, y, width, height, color) -> new ParticleSpawnPos(x, y, width, height)
					);
					ParticleSpawnArea area = new ParticleSpawnArea(pixels.toArray(IParticleSpawnPos[]::new));

					spawnArea = new ParticleSpawnAreaId(spawnAreaId);
					spawnArea.setArea(area);
					spawnArea.setInitialized(true);
				} else {
					spawnArea = ParticleSpawnAreaId.STANDARD_SPAWN_AREA_ID;
				}

				ParticleHolder familyHolder = new ParticleHolder(
						"Family/UnknownParticle@" + RandomSource.create().nextIntBetweenInclusive(0, 100000),
						Either.left(new CachedItem(item)),
						NbtNodeMatch.ANY,
						new HashSet<>(),
						spawnArea,
						particle.getSpawnCount(),
						particle.getSpawnFrequency(),
						particle.getColorProvider(),
						particle.getSpeedCoefficient()
				);
				ParticleSpawner spawner = familyHolder.createSpawner(copy::createParticle);
				list.add(spawner);
			}
		}

		COMBINED_MAP.put(item, list);

		return list;
	}

	@Nullable
	public static List<IParticleSpawner> getSpawnersForItem(Item item) {
		return COMBINED_MAP.get(item);
	}

}
