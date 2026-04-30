package net.lopymine.ip.resourcepack.manager;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.spawner.*;
import net.lopymine.ip.element.predicate.nbt.NbtNodeMatch;
import net.lopymine.ip.family.*;
import net.lopymine.ip.family.FamilyParticleData.GeneratedTextures;
import net.lopymine.ip.family.atlas.AtlasSprite;
import net.lopymine.ip.family.atlas.manager.*;
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

	public static ReloadInfo RELOAD_INFO = new ReloadInfo();

	private static final AtomicInteger VERSION = new AtomicInteger(0);
	private static volatile Map<Item, List<IParticleSpawner>> COMBINED_MAP = new IdentityHashMap<>();

	private static final ParticlesConfigsManager INSTANCE = new ParticlesConfigsManager();

	private ParticlesConfigsManager() {
	}

	public static ParticlesConfigsManager getInstance() {
		return INSTANCE;
	}

	public static void updateCombinedMap() {
		int currentVersion = VERSION.incrementAndGet();

		InventoryParticlesConfig.getInstance().getWhitelistsConfig().recompileAll();
		Set<Entry<ResourceKey<Item>, Item>> entries = new HashSet<>(BuiltInRegistries.ITEM.entrySet());
		ReloadInfo reloadInfo = new ReloadInfo();

		COMBINED_MAP = new IdentityHashMap<>();
		RELOAD_INFO = reloadInfo;
		boolean debug = Boolean.getBoolean("inventory_particles.debug_generate");

		CompletableFuture.supplyAsync(() -> {
			InventoryParticles.LOGGER.info("Started linking particle configs for world items...");
			ReloadData reloadData = new ReloadData(currentVersion);

			reloadInfo.setProgress(0);
			RELOAD_INFO.setTotalItems(entries.size());

			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				if (VERSION.get() != reloadData.getVersion() || Minecraft.getInstance().level == null) {
					InventoryParticles.LOGGER.warn("Canceled linking particle configs for world items.");
					return null;
				}
				Identifier id = entry.getKey().identifier();
				Item item = entry.getValue();

				reloadInfo.setCurrentItem(id.toString());
				getInstance().getItemSpawners(debug, id, item, reloadData);
				reloadInfo.setProgress(reloadInfo.getProgress()+1);
			}

			InventoryParticles.LOGGER.info("Finished linking particle configs for world items!");

			return reloadData;
		}).whenComplete((reloadData, throwable) -> {
			if (throwable != null) {
				InventoryParticlesClient.LOGGER.error("Failed to link particle configs for world items:", throwable);
				return;
			}
			if (reloadData == null || Minecraft.getInstance().level == null) {
				return;
			}

			Map<Item, List<IParticleSpawner>> combinedMap = new IdentityHashMap<>(reloadData.getSpawners());

			if (VERSION.get() != reloadData.getVersion()) {
				return;
			}
			COMBINED_MAP = combinedMap;

			Set<AtlasSprite> sprites = FamilyParticlesAtlasSpriteManager.createSpritesFromGeneratedTextures();
			FamilyParticlesAtlasManager.stitchAndUpdate(sprites, () -> {
				combinedMap.putAll(reloadData.getFamilySpawners());
			});
		});
	}

	@SuppressWarnings("deprecation")
	private void getItemSpawners(boolean debug, Identifier itemId, Item item, ReloadData reloadData) {
		if (debug) {
			List<IParticleSpawner> familyParticles = getFamilyParticles(itemId, item);
			if (familyParticles != null) {
				reloadData.getFamilySpawners().put(item, familyParticles);
			}
			return;
		}

		List<IParticleSpawner> spawners = new ArrayList<>();

		List<IParticleSpawner> specificSpawners = PER_ITEM_PARTICLE_SPAWNERS.get(item);
		boolean bl = specificSpawners != null && !specificSpawners.isEmpty();
		if (bl) {
			spawners.addAll(specificSpawners);
		}

		spawners.addAll(item.builtInRegistryHolder()
				.tags()
				.map(PER_TAG_PARTICLE_SPAWNERS::get)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.toList());

		if (!itemId.getNamespace().equals("minecraft") && !bl) {
			List<IParticleSpawner> familyParticles = getFamilyParticles(itemId, item);
			if (familyParticles != null) {
				reloadData.getFamilySpawners().put(item, familyParticles);
			}
		}

		reloadData.getSpawners().put(item, spawners);
	}

	@Nullable
	private List<IParticleSpawner> getFamilyParticles(Identifier itemId, Item item) {
		List<FamilyParticleConfig> family = FamilyParticlesManager.getFamily(item);
		if (family.isEmpty()) {
			return null;
		}

		for (FamilyParticleConfig config : family) {
			ArrayList<FamilyParticleData> particles = config.getParticles();
			if (particles.isEmpty()) {
				getInstance().getLogger().error("There are no particles in \"{}\" family! Skipping it for item \"{}\"", config.getLocation(), itemId);
				continue;
			}

			List<IParticleSpawner> list = new ArrayList<>();
			for (FamilyParticleData particle : particles) {
				Identifier id = InventoryParticles.id("%s/%s.json".formatted(getInstance().getFolderName(), particle.getId().getPath()));
				List<ParticleConfig> configs = REGISTERED_CONFIGS.get(id);
				if (configs == null || configs.isEmpty()) {
					getInstance().getLogger().error("Failed to find config from \"%s\" for family config from \"%s\"!".formatted(id.getPath(), config.getLocation()));
					continue;
				}

				RenderedItemImage renderedItemImage = ItemRenderingManager.getRenderedItemImage(item, itemId, particle.getTextureExtractMode());
				if (renderedItemImage == null) {
					continue;
				}
				GeneratedTextures generatedTextures = particle.getGeneratedTextures(renderedItemImage, itemId, item);

				for (ParticleConfig particleConfig : configs) {
					ParticleConfig copy = particleConfig.copy();

					if (!generatedTextures.textures().isEmpty() && copy.getTextures().isEmpty()) {
						copy.setTextures(generatedTextures.textures());
					}

					Identifier spawnAreaId = InventoryParticles.id("rii/" + itemId.getPath());
					List<ParticleSpawnPos> pixels = Texture2ObjectsManager.readFromTexture(
							renderedItemImage.getImage(),
							spawnAreaId,
							"family config spawn area",
							() -> (x, y, imageWidth, imageHeight, color) -> {
								if (ArgbUtils2.getAlpha(color) == 0) {
									return false;
								}
								ArrayList<Integer> colors = generatedTextures.colors();
								if (colors.isEmpty()) {
									return true;
								}
								for (Integer c : colors) {
									int distance = ArgbUtils2.colorDistanceSquared(c, color);
									if (distance <= 70 * 70) {
										return true;
									}
								}
								return false;
							},
							(x, y, width, height, color) -> new ParticleSpawnPos(x, y, width, height)
					);

					ParticleSpawnAreaId spawnArea = new ParticleSpawnAreaId(spawnAreaId);
					spawnArea.setArea(new ParticleSpawnArea(pixels.toArray(IParticleSpawnPos[]::new)));
					spawnArea.setInitialized(true);

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

			if (list.isEmpty()) {
				continue;
			}

			return list;
		}

		return null;
	}

	@Nullable
	public static List<IParticleSpawner> getSpawnersForItem(Item item) {
		return COMBINED_MAP.get(item);
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
		VERSION.incrementAndGet();
		COMBINED_MAP = new IdentityHashMap<>();

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

	@Getter
	public static class ReloadData {

		private final int version;
		private final Map<Item, List<IParticleSpawner>> spawners = new IdentityHashMap<>();
		private final Map<Item, List<IParticleSpawner>> familySpawners = new IdentityHashMap<>();

		public ReloadData(int version) {
			this.version = version;
		}
	}

	@Getter
	@Setter
	public static class ReloadInfo {

		private int totalItems = -1;
		private int progress = -1;
		private String currentItem = "air";

	}

}
