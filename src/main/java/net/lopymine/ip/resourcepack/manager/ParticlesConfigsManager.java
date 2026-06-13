package net.lopymine.ip.resourcepack.manager;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.spawner.*;
import net.lopymine.ip.element.predicate.nbt.NbtNodeMatch;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.*;
import net.lopymine.ip.family.FamilyParticleData.GeneratedTextures;
import net.lopymine.ip.family.atlas.AtlasSprite;
import net.lopymine.ip.family.atlas.manager.*;
import net.lopymine.ip.family.cache.*;
import net.lopymine.ip.family.generation.*;
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
import org.jetbrains.annotations.*;

public class ParticlesConfigsManager extends AbstractConfigsManager<ParticleConfig> {

	public static final Map<Identifier, List<ParticleConfig>> REGISTERED_CONFIGS = new HashMap<>();
	private static final Map<Item, List<IParticleSpawner>> PER_ITEM_PARTICLE_SPAWNERS = new IdentityHashMap<>();
	private static final Map<TagKey<Item>, List<IParticleSpawner>> PER_TAG_PARTICLE_SPAWNERS = new HashMap<>();
	public static final ParticleTexturesData EMPTY_PARTICLES_TEXTURES_DATA = new ParticleTexturesData(new GeneratedTextures(new ArrayList<>(), new ArrayList<>()), null);

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
		boolean debug = Boolean.getBoolean("inventory_particles.debug_generate");
		int currentVersion = VERSION.incrementAndGet();

		InventoryParticlesConfig.getInstance().getWhitelistsConfig().recompileAll();
		Set<Entry<ResourceKey<Item>, Item>> entries = new HashSet<>(BuiltInRegistries.ITEM.entrySet());
		COMBINED_MAP = new IdentityHashMap<>();

		Map<Boolean, List<Entry<ResourceKey<Item>, Item>>> map = entries.stream().collect(Collectors.partitioningBy(
				(entry) -> entry.getKey().identifier().getNamespace().equals("minecraft")
		));

		InventoryParticlesClient.sendNoticeMessage(map.get(false).size());

		ReloadInfo reloadInfo = new ReloadInfo();
		RELOAD_INFO = reloadInfo;

		startLinkingFuture(currentVersion, "VANILLA", reloadInfo, map.get(true), debug).handle((reloadData, throwable) -> {
			if (throwable != null) {
				InventoryParticlesClient.LOGGER.error("Failed to link particle configs for VANILLA items:", throwable);
				return null;
			}
			if (reloadData == null || Minecraft.getInstance().level == null) {
				return null;
			}
			Map<Item, List<IParticleSpawner>> combinedMap = new IdentityHashMap<>(reloadData.getSpawners());
			if (VERSION.get() != reloadData.getVersion()) {
				return null;
			}
			COMBINED_MAP = combinedMap;
			return new Pair<>(combinedMap, reloadData);
		}).thenCompose((pair) -> {
			if (pair == null) {
				return CompletableFuture.completedFuture(null);
			}
			Map<Item, List<IParticleSpawner>> combinedMap = pair.getFirst();
			ReloadData vanillaReloadData = pair.getSecond();
			reloadInfo.setModdedItems(true);
			return startLinkingFuture(currentVersion, "MODDED", reloadInfo, map.get(false), debug).whenComplete((reloadData, throwable) -> {
				if (throwable != null) {
					InventoryParticlesClient.LOGGER.error("Failed to link particle configs for MODDED items:", throwable);
					return;
				}
				if (reloadData == null || Minecraft.getInstance().level == null || VERSION.get() != reloadData.getVersion()) {
					return;
				}

				List<CompletableFuture<Void>> futures = new ArrayList<>();

				Map<String, Set<AtlasSprite>> sprites = FamilyParticlesAtlasSpriteManager.createSpritesFromGeneratedTextures();
				for (Entry<String, Set<AtlasSprite>> entry : sprites.entrySet()) {
					String atlasId = entry.getKey();
					FamilyParticlesAtlasManager manager = FamilyParticlesAtlasManager.get(atlasId);
					if (manager == null) {
						continue;
					}

					CompletableFuture<Void> future = new CompletableFuture<>();
					manager.stitchAndUpdate(entry.getValue(), (successful) -> {
						future.complete(null);
						if (successful) {
							FamilyParticlesAtlasCacheManager.save(atlasId);
							FamilyParticlesSpawnAreasCacheManager.save(atlasId);
						}
					});
					futures.add(future);
				}

				CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
					Map<Item, List<IParticleSpawner>> combinedMap2 = new IdentityHashMap<>(combinedMap);
					combinedMap2.putAll(reloadData.getFamilySpawners());
					if (debug) {
						combinedMap2.putAll(vanillaReloadData.getFamilySpawners());
					}
					if (Minecraft.getInstance().level == null || VERSION.get() != reloadData.getVersion()) {
						return;
					}
					COMBINED_MAP = combinedMap2;
				});
			});
		}).exceptionally(throwable -> {
			InventoryParticlesClient.LOGGER.error("Failed to update combined particle map:", throwable);
			return null;
		});
	}

	private static @NonNull CompletableFuture<ReloadData> startLinkingFuture(int currentVersion, String stage, ReloadInfo reloadInfo, Collection<Entry<ResourceKey<Item>, Item>> entries, boolean debug) {
		return CompletableFuture.supplyAsync(() -> {
			InventoryParticles.LOGGER.info("Started linking particle configs for {} items...", stage);
			ReloadData reloadData = new ReloadData(currentVersion);

			reloadInfo.setProgress(0);
			reloadInfo.setTotalItems(entries.size());

			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				if (VERSION.get() != reloadData.getVersion() || Minecraft.getInstance().level == null) {
					InventoryParticles.LOGGER.warn("Canceled linking particle configs for {} items.", stage);
					return null;
				}
				Identifier id = entry.getKey().identifier();
				Item item = entry.getValue();

				reloadInfo.setCurrentItem(id.toString());
				long before = System.currentTimeMillis();
				getItemSpawners(debug, id, item, reloadData);
				long after = System.currentTimeMillis();
				reloadInfo.getLastProcessedItemsTime().add(after - before);
				reloadInfo.setProgress(reloadInfo.getProgress() + 1);
			}

			InventoryParticles.LOGGER.info("Finished linking particle configs for {} items!", stage);
			return reloadData;
		});
	}

	@SuppressWarnings("deprecation")
	private static void getItemSpawners(boolean debug, Identifier itemId, Item item, ReloadData reloadData) {
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
	private static List<IParticleSpawner> getFamilyParticles(Identifier itemId, Item item) {
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

				ParticleTexturesData particleTexturesData = getParticleTexturesData(itemId, item, particle);
				if (particleTexturesData == null) {
					continue;
				}

				Identifier spawnAreaId = InventoryParticles.id("rii/" + itemId.getPath());
				ParticleSpawnAreaId spawnArea = new ParticleSpawnAreaId(spawnAreaId);
				ParticleSpawnArea particleSpawnPos = getParticleSpawnPos(itemId, particleTexturesData, spawnAreaId);
				if ((particleSpawnPos == null || particleSpawnPos.isEmpty()) && particle.getSpawnAreaFallback() != ParticleSpawnAreaId.STANDARD_SPAWN_AREA_ID) {
					particleSpawnPos = particle.getSpawnAreaFallback().getArea();
				}

				spawnArea.setArea(particleSpawnPos);
				spawnArea.setInitialized(true);

				for (ParticleConfig particleConfig : configs) {
					ParticleConfig copy = particleConfig.copy();

					ArrayList<ITexture> textures = particleTexturesData.generatedTextures().textures();
					if (!textures.isEmpty() && copy.getTextures().isEmpty()) {
						copy.setTextures(textures);
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

			if (list.isEmpty()) {
				continue;
			}

			return list;
		}

		return null;
	}

	@Nullable
	private static ParticleSpawnArea getParticleSpawnPos(Identifier itemId, ParticleTexturesData data, Identifier spawnAreaId) {
		List<ParticleSpawnPos> load = FamilyParticlesSpawnAreasCacheManager.load(itemId);
		ArrayList<Integer> colors = data.generatedTextures().colors();
		RenderedItemImage renderedItemImage = data.renderedItemImage();

		if (load == null && colors != null && renderedItemImage != null) {
			List<ParticleSpawnPos> pixels = Texture2ObjectsManager.readFromTexture(
					renderedItemImage.getImage(),
					spawnAreaId,
					"family config spawn area",
					() -> (x, y, imageWidth, imageHeight, color) -> {
						if (ArgbUtils2.getAlpha(color) == 0) {
							return false;
						}
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
			FamilyParticlesSpawnAreasCacheManager.add(itemId, pixels);
			return new ParticleSpawnArea(pixels.toArray(IParticleSpawnPos[]::new));
		}
		if (load != null) {
			if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
				InventoryParticlesClient.LOGGER.info("Found cached spawn area for {}", itemId);
			}
			return new ParticleSpawnArea(load.toArray(IParticleSpawnPos[]::new));
		}
		return null;
	}

	@Nullable
	private static ParticleTexturesData getParticleTexturesData(Identifier itemId, Item item, FamilyParticleData particle) {
		List<Identifier> cachedItemTextures = FamilyParticlesAtlasCacheManager.getOrLoadItemTextures(itemId);
		if (cachedItemTextures == null) {
			if (!particle.canGenerateTextures()) {
				return EMPTY_PARTICLES_TEXTURES_DATA;
			}
			if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
				InventoryParticlesClient.LOGGER.info("[1] Generating textures for {}", itemId);
			}
			RenderedItemImage renderedItemImage = ItemRenderingManager.getRenderedItemImage(item, itemId, particle.getTextureExtractMode());
			if (renderedItemImage == null) {
				return null;
			}
			GeneratedTextures generatedTextures = particle.generateTextures(renderedItemImage, itemId, item);
			return new ParticleTexturesData(generatedTextures, renderedItemImage);
		} else {
			if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
				InventoryParticlesClient.LOGGER.info("[2] Found cached textures for {}", itemId);
			}

			RenderedItemImage specialRenderedItemImage = ItemRenderingManager.getRenderedImageIfSpecial(itemId, item, particle.getTextureExtractMode());

			ArrayList<ITexture> textures = new ArrayList<>();
			cachedItemTextures.sort(Comparator.comparingInt(ParticlesConfigsManager::getTextureNumber));

			FamilyParticlesAtlasManager manager = FamilyParticlesAtlasManager.getOrCreate(itemId.getNamespace());

			for (Identifier cachedTexture : cachedItemTextures) {
				ColoredAtlasTexture directTexture = new ColoredAtlasTexture(
						cachedTexture,
						manager.getAtlasId(),
						(c) -> specialRenderedItemImage == null ? c : specialRenderedItemImage.getColor(c)
				);
				textures.add(directTexture);
			}
			GeneratedTextures generatedTextures = new GeneratedTextures(textures, new ArrayList<>());
			return new ParticleTexturesData(generatedTextures, null);
		}
	}

	private static int getTextureNumber(Identifier id1) {
		String path = id1.getPath();
		String order = path.substring(path.lastIndexOf("_") + 1).replace(".png", "");
		try {
			return Integer.parseInt(order);
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	public record ParticleTexturesData(@NotNull GeneratedTextures generatedTextures, @Nullable RenderedItemImage renderedItemImage) {

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

		private boolean moddedItems = false;
		private int totalItems = -1;
		private int progress = -1;
		private String currentItem = "air";
		private FixedSizeLongQueue lastProcessedItemsTime = new FixedSizeLongQueue(10);

	}

	public static class FixedSizeLongQueue {

		private final int capacity;
		private final Deque<Long> deque;
		private long sum = 0L;

		@Getter
		private volatile double averageSeconds = 0.0;

		public FixedSizeLongQueue(int capacity) {
			if (capacity <= 0) {
				throw new IllegalArgumentException("Capacity must be positive");
			}

			this.capacity = capacity;
			this.deque = new ArrayDeque<>(capacity);
		}

		public synchronized void add(long value) {
			if (this.deque.size() == this.capacity) {
				long removed = this.deque.removeFirst();
				this.sum -= removed;
			}

			this.deque.addLast(value);
			this.sum += value;

			this.averageSeconds = ((double) this.sum / this.deque.size()) / 1000.0;
		}

	}

}
