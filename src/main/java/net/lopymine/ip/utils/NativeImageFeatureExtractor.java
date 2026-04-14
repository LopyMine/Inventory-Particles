package net.lopymine.ip.utils;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import net.lopymine.mossylib.utils.ArgbUtils;

@SuppressWarnings("unused")
public final class NativeImageFeatureExtractor {

    private NativeImageFeatureExtractor() {}

    public static final int DEFAULT_ALPHA_THRESHOLD = 1;
    public static final int DEFAULT_EDGE_THRESHOLD = 80;
    public static final int DEFAULT_HOG_BINS = 9;


    public static double[] averageRgb(NativeImage image) {
        return averageRgb(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double[] averageRgb(NativeImage image, int alphaThreshold) {
        long sumR = 0, sumG = 0, sumB = 0, count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int a = alpha(image, x, y);
                if (a >= alphaThreshold) {
                    sumR += red(image, x, y);
                    sumG += green(image, x, y);
                    sumB += blue(image, x, y);
                    count++;
                }
            }
        }
        if (count == 0) return new double[]{0, 0, 0};
        return new double[]{sumR / (double) count, sumG / (double) count, sumB / (double) count};
    }

    public static double brightness(NativeImage image) {
        return brightness(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double brightness(NativeImage image, int alphaThreshold) {
        double sum = 0;
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int a = alpha(image, x, y);
                if (a >= alphaThreshold) {
                    sum += luminance(red(image, x, y), green(image, x, y), blue(image, x, y));
                    count++;
                }
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    public static double saturation(NativeImage image) {
        return saturation(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double saturation(NativeImage image, int alphaThreshold) {
        double sum = 0;
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int a = alpha(image, x, y);
                if (a >= alphaThreshold) {
                    int r = red(image, x, y);
                    int g = green(image, x, y);
                    int b = blue(image, x, y);
                    sum += hsvSaturation(r, g, b);
                    count++;
                }
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    public static double transparentPixelRatio(NativeImage image) {
        return transparentPixelRatio(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double transparentPixelRatio(NativeImage image, int alphaThreshold) {
        long transparent = 0;
        long total = (long) image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) < alphaThreshold) transparent++;
            }
        }
        return total == 0 ? 0 : transparent / (double) total;
    }

    public static int objectArea(NativeImage image) {
        return objectArea(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static int objectArea(NativeImage image, int alphaThreshold) {
        int area = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) >= alphaThreshold) area++;
            }
        }
        return area;
    }

    public static int connectedComponents(NativeImage image) {
        return connectedComponents(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static int connectedComponents(NativeImage image, int alphaThreshold) {
        int w = image.getWidth();
        int h = image.getHeight();
        boolean[][] visited = new boolean[h][w];
        int components = 0;

        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (visited[y][x] || alpha(image, x, y) < alphaThreshold) continue;

                components++;
                visited[y][x] = true;
                queue.add(new int[]{x, y});

                while (!queue.isEmpty()) {
                    int[] p = queue.removeFirst();
                    int px = p[0], py = p[1];

                    for (int k = 0; k < 8; k++) {
                        int nx = px + dx[k];
                        int ny = py + dy[k];
                        if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                        if (visited[ny][nx]) continue;
                        if (alpha(image, nx, ny) < alphaThreshold) continue;
                        visited[ny][nx] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return components;
    }

    public static double aspectRatio(NativeImage image) {
        return aspectRatio(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double aspectRatio(NativeImage image, int alphaThreshold) {
        Bounds b = foregroundBounds(image, alphaThreshold);
        if (!b.exists()) return 0.0;
        int bw = b.maxX - b.minX + 1;
        int bh = b.maxY - b.minY + 1;
        return bh == 0 ? 0.0 : bw / (double) bh;
    }

    public static double compactness(NativeImage image) {
        return compactness(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double compactness(NativeImage image, int alphaThreshold) {
        int w = image.getWidth();
        int h = image.getHeight();

        long area = 0;
        long perimeter = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (alpha(image, x, y) < alphaThreshold) continue;

                area++;

                if (!alphaAt(image, x - 1, y, alphaThreshold)) perimeter++;
                if (!alphaAt(image, x + 1, y, alphaThreshold)) perimeter++;
                if (!alphaAt(image, x, y - 1, alphaThreshold)) perimeter++;
                if (!alphaAt(image, x, y + 1, alphaThreshold)) perimeter++;
            }
        }

        if (area == 0 || perimeter == 0) return 0.0;
        return (4.0 * Math.PI * area) / (perimeter * perimeter);
    }

    public static double edgeDensity(NativeImage image) {
        return edgeDensity(image, DEFAULT_ALPHA_THRESHOLD, DEFAULT_EDGE_THRESHOLD);
    }

    public static double edgeDensity(NativeImage image, int alphaThreshold, int gradientThreshold) {
        int w = image.getWidth();
        int h = image.getHeight();
        long edgePixels = 0;
        long total = (long) w * h;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (alpha(image, x, y) < alphaThreshold) continue;

                double g00 = luminanceAt(image, x - 1, y - 1);
                double g10 = luminanceAt(image, x,     y - 1);
                double g20 = luminanceAt(image, x + 1, y - 1);

                double g01 = luminanceAt(image, x - 1, y);
                double g21 = luminanceAt(image, x + 1, y);

                double g02 = luminanceAt(image, x - 1, y + 1);
                double g12 = luminanceAt(image, x,     y + 1);
                double g22 = luminanceAt(image, x + 1, y + 1);

                double gx = (-1 * g00) + (1 * g20)
                          + (-2 * g01) + (2 * g21)
                          + (-1 * g02) + (1 * g22);

                double gy = (-1 * g00) + (-2 * g10) + (-1 * g20)
                          + ( 1 * g02) + ( 2 * g12) + ( 1 * g22);

                double magnitude = Math.hypot(gx, gy);
                if (magnitude >= gradientThreshold) edgePixels++;
            }
        }

        return total == 0 ? 0.0 : edgePixels / (double) total;
    }

    public static double[] simplifiedHog(NativeImage image) {
        return simplifiedHog(image, DEFAULT_ALPHA_THRESHOLD, DEFAULT_HOG_BINS);
    }

    public static double[] simplifiedHog(NativeImage image, int alphaThreshold, int bins) {
        double[] hist = new double[bins];
        int w = image.getWidth();
        int h = image.getHeight();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (alpha(image, x, y) < alphaThreshold) continue;

                double gx = luminanceAt(image, x + 1, y) - luminanceAt(image, x - 1, y);
                double gy = luminanceAt(image, x, y + 1) - luminanceAt(image, x, y - 1);

                double magnitude = Math.hypot(gx, gy);
                if (magnitude == 0) continue;

                double angle = Math.toDegrees(Math.atan2(gy, gx));
                if (angle < 0) angle += 180.0; // unsigned gradients: 0..180

                double binPos = angle / 180.0 * bins;
                int bin0 = ((int) Math.floor(binPos)) % bins;
                int bin1 = (bin0 + 1) % bins;
                double t = binPos - Math.floor(binPos);

                hist[bin0] += magnitude * (1.0 - t);
                hist[bin1] += magnitude * t;
            }
        }

        normalizeInPlace(hist);
        return hist;
    }

    public static double[] lbpHistogram(NativeImage image) {
        return lbpHistogram(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double[] lbpHistogram(NativeImage image, int alphaThreshold) {
        double[] hist = new double[256];
        int w = image.getWidth();
        int h = image.getHeight();

        int[] dx = {-1, 0, 1, 1, 1, 0, -1, -1};
        int[] dy = {-1, -1, -1, 0, 1, 1, 1, 0};

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (alpha(image, x, y) < alphaThreshold) continue;

                double center = luminanceAt(image, x, y);
                int code = 0;
                for (int i = 0; i < 8; i++) {
                    double neighbor = luminanceAt(image, x + dx[i], y + dy[i]);
                    if (neighbor >= center) code |= (1 << i);
                }
                hist[code]++;
            }
        }

        normalizeInPlace(hist);
        return hist;
    }

    public static double textureRoughness(NativeImage image) {
        return textureRoughness(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double textureRoughness(NativeImage image, int alphaThreshold) {
        int w = image.getWidth();
        int h = image.getHeight();

        double sum = 0;
        long count = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (alpha(image, x, y) < alphaThreshold) continue;

                double c = luminanceAt(image, x, y);

                if (x + 1 < w && alpha(image, x + 1, y) >= alphaThreshold) {
                    sum += Math.abs(c - luminanceAt(image, x + 1, y)) / 255.0;
                    count++;
                }
                if (y + 1 < h && alpha(image, x, y + 1) >= alphaThreshold) {
                    sum += Math.abs(c - luminanceAt(image, x, y + 1)) / 255.0;
                    count++;
                }
            }
        }

        return count == 0 ? 0.0 : sum / count;
    }

    public static double contrast(NativeImage image) {
        return contrast(image, DEFAULT_ALPHA_THRESHOLD);
    }

    public static double contrast(NativeImage image, int alphaThreshold) {
        List<Double> values = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) >= alphaThreshold) {
                    values.add(luminance(red(image, x, y), green(image, x, y), blue(image, x, y)));
                }
            }
        }
        if (values.isEmpty()) return 0.0;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double var = 0.0;
        for (double v : values) {
            double d = v - mean;
            var += d * d;
        }
        var /= values.size();
        return Math.sqrt(var);
    }

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
        return ArgbUtils.getRed(image.getPixel(x, y));
    }

    private static int green(NativeImage image, int x, int y) {
	    return ArgbUtils.getGreen(image.getPixel(x, y));
    }

    private static int blue(NativeImage image, int x, int y) {
	    return ArgbUtils.getBlue(image.getPixel(x, y));
    }

    private static int alpha(NativeImage image, int x, int y) {
	    return ArgbUtils.getAlpha(image.getPixel(x, y));
    }

    private static boolean alphaAt(NativeImage image, int x, int y, int alphaThreshold) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return false;
        return alpha(image, x, y) >= alphaThreshold;
    }

    private static double luminanceAt(NativeImage image, int x, int y) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return 0.0;
        return luminance(red(image, x, y), green(image, x, y), blue(image, x, y));
    }

    private static double luminance(int r, int g, int b) {
        return 0.299 * r + 0.587 * g + 0.114 * b;
    }

    private static double hsvSaturation(int r, int g, int b) {
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        if (max == 0) return 0.0;
        return (max - min) / (double) max;
    }

    private static void normalizeInPlace(double[] arr) {
        double sum = 0.0;
        for (double v : arr) sum += v;
        if (sum == 0.0) return;
        for (int i = 0; i < arr.length; i++) arr[i] /= sum;
    }

    private static Bounds foregroundBounds(NativeImage image, int alphaThreshold) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) >= alphaThreshold) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

	private record Bounds(int minX, int minY, int maxX, int maxY) {

		boolean exists() {
			return maxX >= 0 && maxY >= 0;
		}
	}
}