package net.lopymine.ip.utils;

import net.minecraft.util.*;

public class ArgbUtils2 {

	public static int lerp(float progress, int first, int second) {
		int alpha = Mth.lerpInt(progress, getAlpha(first), getAlpha(second));
		int red = Mth.lerpInt(progress, getRed(first), getRed(second));
		int green = Mth.lerpInt(progress, getGreen(first), getGreen(second));
		int blue = Mth.lerpInt(progress, getBlue(first), getBlue(second));
		return getArgb(alpha, red, green, blue);
	}

	public static int mix(Integer[] array) {
		if (array.length == 0) {
			return -1;
		}

		int red = 0;
		int green = 0;
		int blue = 0;
		int count = 0;

		for (Integer color : array) {
			red += ArgbUtils2.getRed(color);
			green += ArgbUtils2.getGreen(color);
			blue += ArgbUtils2.getBlue(color);
			count++;
		}

		return ArgbUtils2.getArgb(255, red / count, green / count, blue / count);
	}

	public static int getAlpha(int argb) {
		return argb >>> 24;
	}

	public static int getRed(int argb) {
		return argb >> 16 & 255;
	}

	public static int getGreen(int argb) {
		return argb >> 8 & 255;
	}

	public static int getBlue(int argb) {
		return argb & 255;
	}

	public static int getArgb(int alpha, int red, int green, int blue) {
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	public static int fullAlpha(int argb) {
		return argb | CommonColors.BLACK;
	}

	public static int colorDistanceSquared(int c1, int c2) {
		int r1 = (c1 >> 16) & 255;
		int g1 = (c1 >> 8) & 255;
		int b1 = c1 & 255;

		int r2 = (c2 >> 16) & 255;
		int g2 = (c2 >> 8) & 255;
		int b2 = c2 & 255;

		int dr = r1 - r2;
		int dg = g1 - g2;
		int db = b1 - b2;

		return dr * dr + dg * dg + db * db;
	}

	public static boolean isGrayscalePixel(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;

		return r == g && g == b;
	}

	public static float luminance(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return 0.2126f * r + 0.7152f * g + 0.0722f * b;
	}

}
