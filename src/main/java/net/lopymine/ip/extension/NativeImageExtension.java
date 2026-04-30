package net.lopymine.ip.extension;

import com.mojang.blaze3d.platform.NativeImage;

public class NativeImageExtension {

	public static void setPixelArgb(NativeImage image, int x, int y, int argb) {
		//? if >=1.21.4 {
		image.setPixel(x, y, argb);
		 //?} else {
		/*int actuallyAbgr = swapRedBlueChannels(argb);
		image.setPixelRGBA(x, y, actuallyAbgr);
		*///?}
	}

	public static int getPixelArgb(NativeImage image, int x, int y) {
		//? if >=1.21.4 {
		return image.getPixel(x, y);
		//?} else {
		/*int actuallyAbgr = image.getPixelRGBA(x, y);
		return swapRedBlueChannels(actuallyAbgr);
		*///?}
	}

	public static int swapRedBlueChannels(int color) {
		return color & -16711936 | (color & 16711680) >> 16 | (color & 255) << 16;
	}

}
