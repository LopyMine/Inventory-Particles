package net.lopymine.ip.utils;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.extension.NativeImageExtension;
import net.lopymine.mossylib.utils.ArgbUtils;

@ExtensionMethod(NativeImageExtension.class)
public final class NativeImageFeatureExtractor {

    private NativeImageFeatureExtractor() {}

    public static final int DEFAULT_ALPHA_THRESHOLD = 1;

    public static double[] dominantColors(NativeImage image, int k) {
        return dominantColors(image, k, DEFAULT_ALPHA_THRESHOLD);
    }

	public static double[] dominantColors(NativeImage image, int k, int alphaThreshold) {
		class Bin {
			long sumA, sumR, sumG, sumB, count;
		}

		Map<Integer, Bin> bins = new HashMap<>();
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int a = alpha(image, x, y);
				if (a < alphaThreshold) continue;

				int r = red(image, x, y);
				int g = green(image, x, y);
				int b = blue(image, x, y);

				int key = ((r >> 4) << 8) | ((g >> 4) << 4) | (b >> 4);
				Bin bin = bins.computeIfAbsent(key, kk -> new Bin());

				bin.sumA += a;
				bin.sumR += r;
				bin.sumG += g;
				bin.sumB += b;
				bin.count++;
			}
		}

		long total = bins.values().stream().mapToLong(b -> b.count).sum();
		if (total == 0 || k <= 0) return new double[0];

		List<Map.Entry<Integer, Bin>> top = bins.entrySet().stream()
				.sorted((a, b) -> Long.compare(b.getValue().count, a.getValue().count))
				.limit(k)
				.toList();

		// A, R, G, B, share
		double[] out = new double[top.size() * 5];
		int idx = 0;

		for (Map.Entry<Integer, Bin> e : top) {
			Bin bin = e.getValue();
			double share = bin.count / (double) total;

			out[idx++] = bin.sumA / (double) bin.count;
			out[idx++] = bin.sumR / (double) bin.count;
			out[idx++] = bin.sumG / (double) bin.count;
			out[idx++] = bin.sumB / (double) bin.count;
			out[idx++] = share;
		}

		return out;
	}

    private static int red(NativeImage image, int x, int y) {
        return ArgbUtils.getRed(image.getPixelArgb(x, y));
    }

    private static int green(NativeImage image, int x, int y) {
	    return ArgbUtils.getGreen(image.getPixelArgb(x, y));
    }

    private static int blue(NativeImage image, int x, int y) {
	    return ArgbUtils.getBlue(image.getPixelArgb(x, y));
    }

    private static int alpha(NativeImage image, int x, int y) {
	    return ArgbUtils.getAlpha(image.getPixelArgb(x, y));
    }

}