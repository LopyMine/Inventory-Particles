package net.lopymine.ip.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

public class NativeImageUtils {

	public static final Map<Item, Map<Integer, List<Integer>>> LIST = new ConcurrentHashMap<>();
	public static final Map<Item, Map<Integer, Integer>> MAP2 = new ConcurrentHashMap<>();

	public static int applyTint(int baseColor, int tintColor) {
		int aBase = (baseColor >>> 24) & 0xFF;
		int rBase = (baseColor >>> 16) & 0xFF;
		int gBase = (baseColor >>> 8) & 0xFF;
		int bBase = baseColor & 0xFF;

		int aTint = (tintColor >>> 24) & 0xFF;
		int rTint = (tintColor >>> 16) & 0xFF;
		int gTint = (tintColor >>> 8) & 0xFF;
		int bTint = tintColor & 0xFF;

		int a = aBase;
		int r = (rBase * rTint) / 255;
		int g = (gBase * gTint) / 255;
		int b = (bBase * bTint) / 255;

		return (clampColor(a) << 24)
				| (clampColor(r) << 16)
				| (clampColor(g) << 8)
				| clampColor(b);
	}

	public static int clampColor(int v) {
		return Math.max(0, Math.min(255, v));
	}

	public static NativeImageAndColor luminanceReplace(NativeImage image, NativeImage source, Item id) {
		int width = image.getWidth();
		int height = image.getHeight();

		NativeImage result = new NativeImage(width, height, true);
		if (width <= 0 || height <= 0) {
			return new NativeImageAndColor(result, -1);
		}

		Map<Integer, List<Integer>> pixelAndColors = new HashMap<>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int imagePixel = image.getPixel(x, y);
				if (ArgbUtils.getAlpha(imagePixel) == 0) {
					continue;
				}
				if (pixelAndColors.get(imagePixel) != null) {
					continue;
				}
				List<Integer> pixels = getBestPixelsByLuminance(source, ArgbUtils2.luminance(imagePixel), imagePixel);
				List<Integer> value = pixels.isEmpty() ? List.of(imagePixel) : pixels;
				pixelAndColors.put(imagePixel, value);

				Map<Integer, List<Integer>> map = LIST.computeIfAbsent(id, (key) -> new HashMap<>());
				map.putIfAbsent(imagePixel, value);
			}
		}

		ArrayList<Entry<Integer, List<Integer>>> entries = new ArrayList<>(pixelAndColors.entrySet());
		if (entries.isEmpty()) {
			return new NativeImageAndColor(result, -1);
		}

		Comparator<Entry<Integer, List<Integer>>> sort = Comparator.<Entry<Integer, List<Integer>>>comparingDouble(
				(e) -> ArgbUtils2.luminance(e.getKey())
		).reversed();
		entries.sort(sort);

		Map<Integer, Integer> resultMap = new HashMap<>();
		int lastReferenceColor = -1;
		Set<Integer> lastColors = new HashSet<>();

		Entry<Integer, List<Integer>> first = entries.get(0);
		if (entries.size() >= 2) {
			for (int d = 0; d < first.getValue().size(); d++) {
				Map<Integer, Integer> map = new HashMap<>();
				int referenceColor = first.getValue().get(d);
				lastReferenceColor = referenceColor;
				lastColors.add(referenceColor);

				for (int i = 1; i < entries.size(); i++) {
					Entry<Integer, List<Integer>> entry = entries.get(i);

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

				if (map.size() == entries.size() - 1) {
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
				int imagePixel = image.getPixel(x, y);
				if (ArgbUtils.getAlpha(imagePixel) == 0) {
					continue;
				}
				Integer color = resultMap.get(imagePixel);
				if (color == null) {
					System.out.println("bruh");
					continue;
				}
				result.setPixel(x, y, color);
			}
		}

		return new NativeImageAndColor(result, lastReferenceColor);
	}

//	public static Entry<Integer, List<Integer>> getBestNextReference(Entry<Integer, List<Integer>> currentEntry, ArrayList<Entry<Integer, List<Integer>>> entries) {
//		for (int d = 0; d < currentEntry.getValue().size(); d++) {
//			Map<Integer, Integer> map = new HashMap<>();
//			int referenceColor = currentEntry.getValue().get(d);
//
//			for (int i = d+1; i < entries.size(); i++) {
//				Entry<Integer, List<Integer>> entry = entries.get(i);
//				for (Integer color : entry.getValue()) {
//					boolean bl = ArgbUtils2.colorDistanceSquared(color, referenceColor) > 60 * 60;
//					if (bl) {
//						continue;
//					}
//					map.put(entry.getKey(), color);
//					break;
//				}
//			}
//
//			if (!map.isEmpty()) {
//				Integer keyColor = currentEntry.getKey();
//				map.put(keyColor, referenceColor);
//				resultMap.putAll(map);
//				break;
//			}
//		}
//	}

	public static List<Integer> getBestPixelsByLuminance(NativeImage source, float targetLuminance, int fallbackColor) {
		int width = source.getWidth();
		int height = source.getHeight();

		if (width <= 0 || height <= 0) {
			return List.of(fallbackColor);
		}

		Set<Integer> set = new HashSet<>();

		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				int color = source.getPixel(x, y);
				if (ArgbUtils2.getAlpha(color) == 0) {
					continue;
				}
//				if (ArgbUtils2.isGrayscalePixel(color)) {
//					continue;
//				}
				set.add(color);
			}
		}

		if (set.isEmpty()) {
			return List.of(fallbackColor);
		}

		ArrayList<Integer> results = new ArrayList<>(set);

		results.sort(Comparator.comparingDouble(c -> Math.abs(ArgbUtils2.luminance(c) - targetLuminance)));

		return results;
	}

	private static long stableSeed(String value) {
		long h = value.hashCode() & 0xffffffffL;
		h ^= (h >>> 16);
		h *= 0x7feb352dL;
		h ^= (h >>> 15);
		h *= 0x846ca68bL;
		h ^= (h >>> 16);
		return h;
	}

	public static NativeImage loadFromResource(Identifier id) {
		Resource resource = Minecraft.getInstance().getResourceManager().getResource(id).orElse(null);
		if (resource == null) {
			AbstractTexture texture = Minecraft.getInstance().getTextureManager().byPath.get(id);

			if (!(texture instanceof DynamicTexture backedTexture)) {
				InventoryParticles.LOGGER.error("Failed to register mod's texture as a sprite in atlas! Failed to find texture even from TextureManager! Id: \"{}\", Texture Class: \"{}\"", id, texture == null ? "null" : texture.getClass().getSimpleName());
				return null;
			}

			NativeImage image = backedTexture.getPixels();
			if (image == null) {
				InventoryParticles.LOGGER.error("Failed to register mod's texture as a sprite in atlas! Found image in TextureManager, but it's null somehow!? Id: \"{}\"", id);
				return null;
			}

			NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
			nativeImage.copyFrom(image);
			return nativeImage;
		}

		try {
			return NativeImage.read(resource.open());
		} catch (IOException e) {
			InventoryParticles.LOGGER.error("Failed to load resource for mod's atlas:", e);
		}

		return null;
	}

	public record NativeImageAndColor(NativeImage image, int averageColor) { }

}
