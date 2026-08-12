package net.lopymine.ip.resourcepack.manager;

import com.mojang.datafixers.util.*;
import com.mojang.serialization.Codec;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
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
import net.lopymine.ip.family.generation.batch.*;
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
			long before = System.currentTimeMillis();

			InventoryParticles.LOGGER.info("Started linking particle configs for {} items...", stage);
			ReloadData reloadData = new ReloadData(currentVersion);

			reloadInfo.setProgress(0);
			reloadInfo.setTotalItems(entries.size());

			Map<String, List<Entry<ResourceKey<Item>, Item>>> groups = getGroupedItems(entries);
			for (List<Entry<ResourceKey<Item>, Item>> group : groups.values()) {
				if (!linkGroup(group, reloadData, reloadInfo, debug)) {
					InventoryParticles.LOGGER.warn("Canceled linking particle configs for {} items.", stage);
					return null;
				}
			}

			long after = System.currentTimeMillis();
			InventoryParticles.LOGGER.info("Finished linking particle configs for {} items! It took {} seconds. Amount: {}", stage, (after - before) / 1000D, entries.size());
			return reloadData;
		});
	}

	private static Map<String, List<Entry<ResourceKey<Item>, Item>>> getGroupedItems(Collection<Entry<ResourceKey<Item>, Item>> entries) {
		Map<String, List<Entry<ResourceKey<Item>, Item>>> groups = new LinkedHashMap<>();
		for (Entry<ResourceKey<Item>, Item> entry : entries) {
			groups.computeIfAbsent(entry.getKey().identifier().getNamespace(), (ignored) -> new ArrayList<>()).add(entry);
		}
		return groups;
	}

	private static boolean linkGroup(List<Entry<ResourceKey<Item>, Item>> entries, ReloadData reloadData, ReloadInfo reloadInfo, boolean debug) {
		if (isLinkingCanceled(reloadData)) {
			return false;
		}

		FamilyLinkCache linkCache = new FamilyLinkCache();
		extractAndRenderFamilyItemImages(linkCache, entries, debug);

		try {
			for (Entry<ResourceKey<Item>, Item> entry : entries) {
				if (isLinkingCanceled(reloadData)) {
					return false;
				}
				Identifier itemId = entry.getKey().identifier();
				Item item = entry.getValue();

				reloadInfo.setCurrentItem(itemId.toString());
				long before = System.currentTimeMillis();
				linkSpawners(itemId, item, reloadData, linkCache, debug);
				long after = System.currentTimeMillis();
				reloadInfo.getLastProcessedItemsTime().add(after - before);
				reloadInfo.setProgress(reloadInfo.getProgress() + 1);
			}
		} finally {
			linkCache.closeAndClear();
		}

		return true;
	}

	private static boolean isLinkingCanceled(ReloadData reloadData) {
		return VERSION.get() != reloadData.getVersion() || Minecraft.getInstance().level == null;
	}

	private static void extractAndRenderFamilyItemImages(FamilyLinkCache cache, List<Entry<ResourceKey<Item>, Item>> entries, boolean debug) {
		List<ItemRenderRequest> renderRequests = new ArrayList<>();

		for (Entry<ResourceKey<Item>, Item> entry : entries) {
			Identifier itemId = entry.getKey().identifier();
			Item item = entry.getValue();

			if (shouldExtractFamilyItemImage(debug, itemId, item)) {
				extractFamilyItemRenderRequests(cache, renderRequests, itemId, item);
			}
		}

		cache.setImages(ItemRenderBatcher.render(renderRequests));
	}

	private static boolean shouldExtractFamilyItemImage(boolean debug, Identifier itemId, Item item) {
		if (debug) {
			return true;
		}
		if (itemId.getNamespace().equals("minecraft")) {
			return false;
		}
		List<IParticleSpawner> specificSpawners = PER_ITEM_PARTICLE_SPAWNERS.get(item);
		return specificSpawners == null || specificSpawners.isEmpty();
	}

	private static void extractFamilyItemRenderRequests(FamilyLinkCache plan, List<ItemRenderRequest> requests, Identifier itemId, Item item) {
		List<FamilyParticleConfig> configs = FamilyParticlesManager.getFamilyConfigsForItem(item);
		if (configs.isEmpty()) {
			return;
		}
		plan.putResolvedFamilyConfigs(item, configs);

		boolean cached = FamilyParticlesAtlasCacheManager.getOrLoadItemTextures(itemId) != null;
		BucketItem bucketItem = getFluidBucket(configs, item, cached);
		if (bucketItem != null) {
			requests.add(new ItemRenderRequest(itemId, item, bucketItem));
		}

		if (!cached && needsItemRender(configs, item)) {
			requests.add(new ItemRenderRequest(itemId, item, null));
		}
	}

	@Nullable
	private static BucketItem getFluidBucket(List<FamilyParticleConfig> configs, Item item, boolean cached) {
		return streamParticles(configs)
				.filter((particle) -> cached || particle.canGenerateTextures())
				.map((particle) -> ItemRenderingManager.resolveBucket(item, particle.getTextureExtractMode()))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private static boolean needsItemRender(List<FamilyParticleConfig> configs, Item item) {
		return streamParticles(configs)
				.filter(FamilyParticleData::canGenerateTextures)
				.anyMatch((particle) -> ItemRenderingManager.resolveBucket(item, particle.getTextureExtractMode()) == null);
	}

	private static Stream<FamilyParticleData> streamParticles(List<FamilyParticleConfig> configs) {
		return configs.stream().flatMap((config) -> config.getParticles().stream());
	}

	@SuppressWarnings("deprecation")
	private static void linkSpawners(Identifier itemId, Item item, ReloadData reloadData, FamilyLinkCache cache, boolean debug) {
		// debug -> all to family
		if (debug) {
			List<IParticleSpawner> familySpawners = extractFamilySpawners(itemId, item, cache);
			if (familySpawners != null) {
				reloadData.getFamilySpawners().put(item, familySpawners);
			}
			return;
		}

		List<IParticleSpawner> spawners = new ArrayList<>();

		// resource packs
		List<IParticleSpawner> specificSpawners = PER_ITEM_PARTICLE_SPAWNERS.get(item);
		boolean bl = specificSpawners != null && !specificSpawners.isEmpty();
		if (bl) {
			spawners.addAll(specificSpawners);
		}

		// tags
		spawners.addAll(item.builtInRegistryHolder()
				.tags()
				.map(PER_TAG_PARTICLE_SPAWNERS::get)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.toList());

		reloadData.getSpawners().put(item, spawners);

		// family
		if (!itemId.getNamespace().equals("minecraft") && !bl) {
			List<IParticleSpawner> familyParticles = extractFamilySpawners(itemId, item, cache);
			if (familyParticles != null) {
				reloadData.getFamilySpawners().put(item, familyParticles);
			}
		}
	}

	@Nullable
	private static List<IParticleSpawner> extractFamilySpawners(Identifier itemId, Item item, FamilyLinkCache cache) {
		List<FamilyParticleConfig> family = cache.getResolvedFamilyConfigs(item);
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

			for (FamilyParticleData particleData : particles) {
				Identifier id = InventoryParticles.id("%s/%s.json".formatted(getInstance().getFolderName(), particleData.getId().getPath()));
				List<ParticleConfig> configs = REGISTERED_CONFIGS.get(id);
				if (configs == null || configs.isEmpty()) {
					getInstance().getLogger().error("Failed to find config from \"%s\" for family config from \"%s\"!".formatted(id.getPath(), config.getLocation()));
					continue;
				}

				ParticleTexturesData particleTexturesData = getParticleTexturesData(itemId, item, particleData, cache);
				if (particleTexturesData == null) {
					continue;
				}

				Identifier spawnAreaId = InventoryParticles.id("rii/" + itemId.getPath());
				ParticleSpawnArea particleSpawnPos = getParticleSpawnPos(itemId, particleTexturesData, spawnAreaId);
				if ((particleSpawnPos == null || particleSpawnPos.isEmpty()) && particleData.getSpawnAreaFallback() != ParticleSpawnAreaId.STANDARD_SPAWN_AREA_ID) {
					particleSpawnPos = particleData.getSpawnAreaFallback().getArea();
				}

				ParticleSpawnAreaId spawnArea = new ParticleSpawnAreaId(spawnAreaId);
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
							particleData.getSpawnCount(),
							particleData.getSpawnFrequency(),
							particleData.getColorProvider(),
							particleData.getSpeedCoefficient()
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
	private static ParticleTexturesData getParticleTexturesData(Identifier itemId, Item item, FamilyParticleData particleData, FamilyLinkCache cache) {
		List<Identifier> cachedItemTextures = FamilyParticlesAtlasCacheManager.getOrLoadItemTextures(itemId);
		RenderedItemImages extractedItemImages = cache.getImages();

		if (cachedItemTextures == null) {
			if (!particleData.canGenerateTextures()) {
				return EMPTY_PARTICLES_TEXTURES_DATA;
			}
			if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
				InventoryParticlesClient.LOGGER.info("[1] Generating textures for {}", itemId);
			}
			RenderedItemImage image = Optional.ofNullable(extractedItemImages.get(item, particleData.getTextureExtractMode())).orElseGet(
					() -> ItemRenderingManager.renderItemImage(item, itemId, particleData.getTextureExtractMode())
			);
			if (image == null) {
				return null;
			}
			GeneratedTextures generatedTextures = particleData.generateFamilyTextures(image, itemId, item);
			return new ParticleTexturesData(generatedTextures, image);
		} else {
			if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled()) {
				InventoryParticlesClient.LOGGER.info("[2] Found cached textures for {}", itemId);
			}

			RenderedItemImage extractedFluid = extractedItemImages.getFluid(item);
			RenderedItemImage specialRenderedItemImage = extractedFluid != null
					?
					extractedFluid
					:
					ItemRenderingManager.renderItemImageIfSpecial(itemId, item, particleData.getTextureExtractMode());

			ArrayList<ITexture> textures = new ArrayList<>();
			cachedItemTextures.sort(Comparator.comparingInt(ParticlesConfigsManager::getTextureNumber)); // bruh

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
		// Templates come from the resources, they cannot outlive a reload.
		TextureGenerationManager.clearTemplates();
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
