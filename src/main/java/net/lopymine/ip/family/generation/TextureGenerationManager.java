package net.lopymine.ip.family.generation;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import net.fabricmc.loader.api.FabricLoader;
import net.lopymine.ip.element.texture.*;
import net.lopymine.ip.family.FamilyParticleData.GeneratedTextures;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.utils.NativeImageUtils.NativeImageAndColor;
import net.lopymine.ip.utils.iac.RenderedItemImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;

public class TextureGenerationManager {

	private static final List<NativeImage> ALL_GENERATED_TEXTURES = new ArrayList<>();

	public static GeneratedTextures colorizeWithDominantColor(RenderedItemImage renderedItemImage, ArrayList<Identifier> textures) {
		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colorsList = new ArrayList<>();

		double[] colors = NativeImageFeatureExtractor.dominantColors(renderedItemImage.getImage(), 1);
		int color = getDominantColor(colors, 0.0F);

		colorsList.add(color);

		Function<Integer, Integer> colorProvider = (c) -> renderedItemImage.getColor(color);

		for (Identifier texture : textures) {
			ColoredAtlasTexture coloredAtlasTexture;

			if (texture.getPath().endsWith(".png")) {
				String path = texture.getPath();
				String fixedPath = path.substring(0, path.length() - 4);
				coloredAtlasTexture = new ColoredAtlasTexture(texture.withPath(fixedPath), null, colorProvider);
			} else {
				coloredAtlasTexture = new ColoredAtlasTexture(texture, null, colorProvider);
			}

			list.add(coloredAtlasTexture);
		}

		return new GeneratedTextures(list, colorsList);
	}

	private static int getDominantColor(double[] colors, @SuppressWarnings("all") float percentOfLight) {
		int argb;
		if (colors.length >= 4) {
			int colorCount = colors.length / 4;
			Integer[] array = new Integer[colorCount];

			for (int i = 0, j = 0; i + 3 < colors.length; i += 4, j++) {
				int color = ArgbUtils2.getArgb(
						(int) colors[i],
						(int) colors[i + 1],
						(int) colors[i + 2],
						(int) colors[i + 3]
				);
				array[j] = color;
			}

			Arrays.sort(array, (a, b) -> {
				float la = ArgbUtils2.luminance(a);
				float lb = ArgbUtils2.luminance(b);
				return Float.compare(la, lb);
			});

			int index = (int) Math.floor((array.length - 1) * percentOfLight);
			argb = array[index];
		} else {
			argb = -1;
		}
		return argb;
	}

	public static GeneratedTextures luminanceReplace(RenderedItemImage renderedItemImage, Identifier itemId, Item item, ArrayList<Identifier> textures) {
		ArrayList<ITexture> list = new ArrayList<>();
		ArrayList<Integer> colors = new ArrayList<>();

		for (Identifier texture : textures) {
			try {
				CompletableFuture<GenerationResult<ITexture>> future = CompletableFuture.supplyAsync(() -> {
					NativeImage nativeImage = NativeImageUtils.loadFromResource(texture.withPrefix("textures/ifamily/"));
					if (nativeImage == null) {
						return null;
					}

					NativeImageAndColor replaced = NativeImageUtils.luminanceReplace(nativeImage, renderedItemImage.getImage(), item);
					NativeImage replacedImage = replaced.image();

					ALL_GENERATED_TEXTURES.add(replacedImage);

					Identifier location = texture.withPrefix(itemId.getPath() + "/");
					Minecraft.getInstance().getTextureManager().register(
							location,
							new DynamicTexture(location::toString, replacedImage)
					);

					debugUpload(renderedItemImage.getImage(), location);

					DirectTexture directTexture = new DirectTexture(
							location,
							replacedImage.getWidth(),
							replacedImage.getHeight(),
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
		for (NativeImage texture : ALL_GENERATED_TEXTURES) {
			texture.close();
		}
		ALL_GENERATED_TEXTURES.clear();
	}

	public record GenerationResult<T>(T object, Integer color) {}

}
