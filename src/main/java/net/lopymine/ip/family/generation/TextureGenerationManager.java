package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.FamilyParticleData.*;
import net.lopymine.ip.family.atlas.manager.*;
import net.lopymine.ip.family.cache.FamilyParticlesCacheManager;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.NativeImageUtils.NativeImageAndColor;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

public class TextureGenerationManager {

	private static final Map<Identifier, List<Identifier>> PER_ITEM_TEXTURES = new ConcurrentHashMap<>();
	private static final Map<Identifier, NativeImage> ALL_GENERATED_TEXTURES = new ConcurrentHashMap<>();

	public static GeneratedTextures generateWithReplace(RenderedItemImage renderedItemImage, Identifier itemId, Item item, ArrayList<Identifier> textures, TextureGenerationMode textureGenerationMode) {
		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		for (Identifier texture : textures) {
			try {
				CompletableFuture<GenerationResult<ITexture>> future = CompletableFuture.supplyAsync(() -> {
					NativeImage particleImage = NativeImageUtils.loadFromResource(texture.withPrefix("textures/ifamily/"));
					if (particleImage == null) {
						return null;
					}
					Identifier particleId = texture.withPrefix(itemId.getPath() + "/");
					NativeImageAndColor generatedParticle = NativeImageUtils.generateWithReplace(particleImage, renderedItemImage.getImage(), item, textureGenerationMode);

					PER_ITEM_TEXTURES.computeIfAbsent(itemId, (key) -> new ArrayList<>()).add(particleId);
					ALL_GENERATED_TEXTURES.put(particleId, generatedParticle.image());
					FamilyParticlesCacheManager.save(InventoryParticles.parseId(itemId.getNamespace() + ":" + particleId.getPath()), generatedParticle.image());

					ColoredAtlasTexture directTexture = new ColoredAtlasTexture(
							particleId,
							FamilyParticlesAtlasManager.ATLAS_ID,
							renderedItemImage::getColor
					);
					return new GenerationResult<>(
							directTexture,
							generatedParticle.averageColor()
					);
				}, Minecraft.getInstance());

				GenerationResult<ITexture> result = future.get();

				if (result == null) {
					throw new NullPointerException("Failed to replace by luminance for \"%s\"".formatted(itemId));
				}

				list.add(result.object);
				colors.add(result.color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new GeneratedTextures(list, colors);
	}

	public static void clear() {
		for (Entry<Identifier, NativeImage> entry : ALL_GENERATED_TEXTURES.entrySet()) {
			Minecraft.getInstance().getTextureManager().release(entry.getKey());
		}
		ALL_GENERATED_TEXTURES.clear();
		PER_ITEM_TEXTURES.clear();
	}

	public static Map<Identifier, NativeImage> getAllGeneratedTextures() {
		return ALL_GENERATED_TEXTURES;
	}

	public static Map<Identifier, List<Identifier>> getPerItemTextures() {
		return PER_ITEM_TEXTURES;
	}

	public static void load(Map<Identifier, NativeImage> map) {
		for (Entry<Identifier, NativeImage> entry : map.entrySet()) {
			Identifier key = entry.getKey();
			NativeImage nativeImage = ALL_GENERATED_TEXTURES.get(key);
			if (nativeImage != null) {
				continue;
			}

			String path = key.getPath();
			String itemIdParsed = path.substring(0, path.indexOf("/"));
			Identifier itemId = InventoryParticles.parseId(key.getNamespace() + ":" + itemIdParsed);

			//? if >=1.21.4 {
			if (BuiltInRegistries.ITEM.get(itemId).isEmpty()) {
				continue;
			}
			//?} else {
			/*if (BuiltInRegistries.ITEM.get(itemId) == Items.AIR) {
				continue;
			}
			*///?}

			Identifier id = InventoryParticles.id(key.getPath());
			PER_ITEM_TEXTURES.computeIfAbsent(itemId, (k) -> new ArrayList<>()).add(id);
			ALL_GENERATED_TEXTURES.put(id, entry.getValue());
		}
	}

	public record GenerationResult<T>(T object, Integer color) {}

}
