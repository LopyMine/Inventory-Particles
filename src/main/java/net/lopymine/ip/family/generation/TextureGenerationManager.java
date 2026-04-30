package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.FamilyParticleData.*;
import net.lopymine.ip.family.atlas.manager.*;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.NativeImageUtils.NativeImageAndColor;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?}

public class TextureGenerationManager {

	private static final Map<Identifier, NativeImage> ALL_GENERATED_TEXTURES = new ConcurrentHashMap<>();

	public static GeneratedTextures generateWithReplace(RenderedItemImage renderedItemImage, Identifier itemId, Item item, ArrayList<Identifier> textures, TextureGenerationMode textureGenerationMode) {
		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		for (Identifier texture : textures) {
			try {
				CompletableFuture<GenerationResult<ITexture>> future = CompletableFuture.supplyAsync(() -> {
					NativeImage nativeImage = NativeImageUtils.loadFromResource(texture.withPrefix("textures/ifamily/"));
					if (nativeImage == null) {
						return null;
					}

					NativeImageAndColor replaced = NativeImageUtils.generateWithReplace(nativeImage, renderedItemImage.getImage(), item, textureGenerationMode);
					NativeImage replacedImage = replaced.image();

					Identifier location = texture.withPrefix(itemId.getPath() + "/");

					debugUpload(renderedItemImage.getImage(), location);

					ALL_GENERATED_TEXTURES.put(location, replacedImage);
					ColoredAtlasTexture directTexture = new ColoredAtlasTexture(
							location,
							FamilyParticlesAtlasManager.ATLAS_ID,
							renderedItemImage::getColor
					);
					return new GenerationResult<>(
							directTexture,
							replaced.averageColor()
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

	private static void debugUpload(NativeImage replaced, Identifier location) {
//		Util.ioPool().execute(() -> {
//			try {
//				replaced.writeToFile(FabricLoader.getInstance().getConfigDir().resolve("images").resolve(location.getPath().replace("/", "_") + ".png"));
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		});
	}

	public static void clear() {
		for (Entry<Identifier, NativeImage> entry : ALL_GENERATED_TEXTURES.entrySet()) {
			Minecraft.getInstance().getTextureManager().release(entry.getKey());
		}
		ALL_GENERATED_TEXTURES.clear();
	}

	public static Map<Identifier, NativeImage> getAllGeneratedTextures() {
		return ALL_GENERATED_TEXTURES;
	}

	public record GenerationResult<T>(T object, Integer color) {}

}
