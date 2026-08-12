package net.lopymine.ip.utils;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.ip.family.FamilyParticleData.TextureGenerationMode;
import net.lopymine.mossylib.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

@ExtensionMethod(NativeImageExtension.class)
public class NativeImageUtils {

	public static final Map<Item, Map<Integer, List<Integer>>> LIST = new ConcurrentHashMap<>();
	public static final Map<Item, Map<Integer, Integer>> MAP2 = new ConcurrentHashMap<>();

	public static int applyTint(int baseColor, int tintColor) {
		int aBase = (baseColor >>> 24) & 0xFF;
		int rBase = (baseColor >>> 16) & 0xFF;
		int gBase = (baseColor >>> 8) & 0xFF;
		int bBase = baseColor & 0xFF;

		int rTint = (tintColor >>> 16) & 0xFF;
		int gTint = (tintColor >>> 8) & 0xFF;
		int bTint = tintColor & 0xFF;

		int r = (rBase * rTint) / 255;
		int g = (gBase * gTint) / 255;
		int b = (bBase * bTint) / 255;

		return (clampColor(aBase) << 24)
				| (clampColor(r) << 16)
				| (clampColor(g) << 8)
				| clampColor(b);
	}

	public static int clampColor(int v) {
		return Math.max(0, Math.min(255, v));
	}

	public static NativeImageAndColor generateWithReplace(NativeImage image, SourceColors sourceColors, Item id) {
		int width = image.getWidth();
		int height = image.getHeight();

		NativeImage result = new NativeImage(width, height, true);
		if (width <= 0 || height <= 0) {
			return new NativeImageAndColor(result, -1);
		}

		Map<Integer, List<Integer>> pixelAndColors = new HashMap<>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int imagePixel = image.getPixelArgb(x, y);
				if (ArgbUtils.getAlpha(imagePixel) == 0) {
					continue;
				}
				if (pixelAndColors.get(imagePixel) != null) {
					continue;
				}
				List<Integer> pixels = sourceColors.getBestPixelsByLuminance(ArgbUtils2.luminance(imagePixel), imagePixel);
				List<Integer> value = pixels.isEmpty() ? List.of(imagePixel) : pixels;
				pixelAndColors.put(imagePixel, value);

				Map<Integer, List<Integer>> map = LIST.computeIfAbsent(id, (key) -> new HashMap<>());
				map.putIfAbsent(imagePixel, value);
			}
		}

		ArrayList<Entry<Integer, List<Integer>>> templateColorAndBestPixels = new ArrayList<>(pixelAndColors.entrySet());
		if (templateColorAndBestPixels.isEmpty()) {
			return new NativeImageAndColor(result, -1);
		}

		Comparator<Entry<Integer, List<Integer>>> sort = Comparator.<Entry<Integer, List<Integer>>>comparingDouble(
				(e) -> ArgbUtils2.luminance(e.getKey())
		).reversed();
		templateColorAndBestPixels.sort(sort);

		Map<Integer, Integer> resultMap = new HashMap<>();
		int lastReferenceColor = -1;
		Set<Integer> lastColors = new HashSet<>();

		Entry<Integer, List<Integer>> first = templateColorAndBestPixels.get(0);
		if (templateColorAndBestPixels.size() >= 2) {
			for (int d = 0; d < first.getValue().size(); d++) {
				Map<Integer, Integer> map = new HashMap<>();
				int referenceColor = first.getValue().get(d);
				lastReferenceColor = referenceColor;
				lastColors.add(referenceColor);

				for (int i = 1; i < templateColorAndBestPixels.size(); i++) {
					Entry<Integer, List<Integer>> entry = templateColorAndBestPixels.get(i);

					int maxDistance = 442;
					int distance = 35;
					boolean found = false;
					while (!found && distance <= maxDistance) {
						for (Integer color : entry.getValue()) {
							if (lastColors.contains(color) || color.equals(lastReferenceColor)) {
								continue;
							}
							boolean bl = ArgbUtils2.colorDistanceSquared(color, lastReferenceColor) > distance * distance;
							if (bl) {
								continue;
							}
							lastReferenceColor = color;
							lastColors.add(referenceColor);
							found = true;
							map.put(entry.getKey(), color);
							break;
						}
						distance+=5;
					}

					if(!found) {
						Integer color = entry.getValue().get(0);
						lastReferenceColor = color;
						lastColors.add(referenceColor);
						map.put(entry.getKey(), color);
					}
				}

				if (map.size() == templateColorAndBestPixels.size() - 1) {
					resultMap.put(first.getKey(), referenceColor);
					resultMap.putAll(map);
					break;
				}
			}
		} else {
			Integer value = first.getValue().get(0);
			resultMap.put(first.getKey(), value);
			lastReferenceColor = value;
		}

		if (resultMap.isEmpty()) {
			System.out.println("bruh2");
			return new NativeImageAndColor(result, -1);
		}

		for (Entry<Integer, Integer> entry : resultMap.entrySet()) {
			Map<Integer, Integer> map = MAP2.computeIfAbsent(id, (key) -> new HashMap<>());
			map.put(entry.getKey(), entry.getValue());
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int imagePixel = image.getPixelArgb(x, y);
				if (ArgbUtils.getAlpha(imagePixel) == 0) {
					continue;
				}
				Integer color = resultMap.get(imagePixel);
				if (color == null) {
					System.out.println("bruh");
					continue;
				}
				result.setPixelArgb(x, y, color);
			}
		}

		return new NativeImageAndColor(result, lastReferenceColor);
	}

	public static SourceColors clusterColors(NativeImage source, TextureGenerationMode textureGenerationMode) {
		int width = source.getWidth();
		int height = source.getHeight();

		if (width <= 0 || height <= 0) {
			return new SourceColors(new ArrayList<>(), textureGenerationMode);
		}

		int groupDifference = 12;

		Map<Integer, ColorCluster> clusters = new HashMap<>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int color = source.getPixelArgb(x, y);
				if (ArgbUtils2.getAlpha(color) == 0) {
					continue;
				}

				int key = createClusterKey(color, groupDifference);

				ColorCluster cluster = clusters.computeIfAbsent(key, k -> new ColorCluster());
				cluster.add(color);
			}
		}

		return new SourceColors(new ArrayList<>(clusters.values()), textureGenerationMode);
	}

	private static int createClusterKey(int color, int bucketSize) {
		int red = ArgbUtils.getRed(color);
		int green = ArgbUtils.getGreen(color);
		int blue = ArgbUtils.getBlue(color);

		int rb = red / bucketSize;
		int gb = green / bucketSize;
		int bb = blue / bucketSize;

		return (rb << 16) | (gb << 8) | bb;
	}

	public static class SourceColors {

		private final ArrayList<ColorCluster> clusters;
		private final TextureGenerationMode textureGenerationMode;

		private SourceColors(ArrayList<ColorCluster> clusters, TextureGenerationMode textureGenerationMode) {
			this.clusters             = clusters;
			this.textureGenerationMode = textureGenerationMode;
		}

		public List<Integer> getBestPixelsByLuminance(float targetLuminance, int fallbackColor) {
			if (this.clusters.isEmpty()) {
				return List.of(fallbackColor);
			}

			ArrayList<ColorCluster> sortClusters = new ArrayList<>(this.clusters);
			for (ColorCluster cluster : sortClusters) {
				cluster.calcScore(targetLuminance, this.textureGenerationMode);
			}

			sortClusters.sort(Comparator.comparingDouble((ColorCluster c) -> c.score).reversed());

			ArrayList<Integer> result = new ArrayList<>(sortClusters.size());
			for (ColorCluster cluster : sortClusters) {
				result.add(cluster.representativeColor);
			}

			return result;
		}
	}

	private static class ColorCluster {

		private final ArrayList<Integer> colors = new ArrayList<>();

		private int representativeColor;
		private double score;

		void add(int color) {
			this.colors.add(color);
		}

		void calcScore(float targetLuminance, TextureGenerationMode textureGenerationMode) {
			if (this.colors.isEmpty()) {
				this.representativeColor = -1;
				this.score = Double.NEGATIVE_INFINITY;
				return;
			}

			int best = this.colors.get(0);
			double bestDifference = Math.abs(ArgbUtils2.luminance(best) - targetLuminance);

			for (int i = 1; i < this.colors.size(); i++) {
				int color = this.colors.get(i);
				double difference = Math.abs(ArgbUtils2.luminance(color) - targetLuminance);
				if (difference < bestDifference) {
					best           = color;
					bestDifference = difference;
				}
			}

			this.representativeColor = best;

			double luminanceScore = 1.0 / (1.0 + bestDifference * 8.0);
			double saturationScore = 0.5 + ArgbUtils2.getSaturation(best);
			double frequencyScore = Math.log1p(this.colors.size());

			double score = 1.0F;
			if (textureGenerationMode.isLuminance()) {
				score *= luminanceScore;
			}
			if (textureGenerationMode.isSaturation()) {
				score *= saturationScore;
			}
			if (textureGenerationMode.isFrequency()) {
				score *= frequencyScore;
			}

			this.score = score;
		}
	}

	public static NativeImage loadFromResource(Identifier id) {
		Resource resource = Minecraft.getInstance().getResourceManager().getResource(id).orElse(null);
		if (resource == null) {
			AbstractTexture texture = Minecraft.getInstance().getTextureManager().byPath.get(id);

			if (!(texture instanceof DynamicTexture backedTexture)) {
				InventoryParticles.LOGGER.error("Failed to find texture from TextureManager! Id: \"{}\", Texture Class: \"{}\"", id, texture == null ? "null" : texture.getClass().getSimpleName());
				return null;
			}

			NativeImage image = backedTexture.getPixels();
			if (image == null) {
				InventoryParticles.LOGGER.error("Found image in TextureManager, but it's null somehow!? Id: \"{}\"", id);
				return null;
			}

			NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
			nativeImage.copyFrom(image);
			return nativeImage;
		}

		try {
			return NativeImage.read(resource.open());
		} catch (IOException e) {
			InventoryParticles.LOGGER.error("Failed to load image! Id: \"{}\", :", id, e);
		}

		return null;
	}

	public record NativeImageAndColor(NativeImage image, int averageColor) { }

	private record PaletteColor(int score, int color) {}

}
