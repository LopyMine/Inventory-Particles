package net.lopymine.ip.utils;

import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;

public class ArgbUtils2 {

	public static int lerp(float progress, int first, int second) {
		int alpha = MathHelper.lerp(progress, getAlpha(first), getAlpha(second));
		int red = MathHelper.lerp(progress, getRed(first), getRed(second));
		int green = MathHelper.lerp(progress, getGreen(first), getGreen(second));
		int blue = MathHelper.lerp(progress, getBlue(first), getBlue(second));
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
		return argb | Colors.BLACK;
	}

}
