package net.lopymine.ip.utils;

public class AbgrUtils {

	public static int getAlpha(int abgr) {
		return abgr >>> 24;
	}

	public static int getRed(int abgr) {
		return abgr & 255;
	}

	public static int getGreen(int abgr) {
		return abgr >> 8 & 255;
	}

	public static int getBlue(int abgr) {
		return abgr >> 16 & 255;
	}

	public static int getAbgr(int a, int b, int g, int r) {
		return a << 24 | b << 16 | g << 8 | r;
	}

	public static int toAbgr(int argb) {
		return argb & -16711936 | (argb & 16711680) >> 16 | (argb & 255) << 16;
	}

}
