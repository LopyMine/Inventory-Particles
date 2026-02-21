package net.lopymine.ip.t2o;

import java.io.InputStream;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.utils.*;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.Identifier;

public class Texture2ObjectsManager {

	public static <T> List<T> readFromTexture(Identifier id, String objectName, Texture2ObjectPixelFilter filter, Texture2Object<T> texture2Object) {
		try {
			Optional<Resource> optional = Minecraft.getInstance().getResourceManager().getResource(id);
			if (optional.isEmpty()) {
				if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled() || MossyLoader.isDevelopmentEnvironment()) {
					InventoryParticlesClient.LOGGER.error("Failed to find texture from \"{}\" to create {} from texture!", id, objectName);
				}
				return List.of();
			}
			Resource resource = optional.get();
			InputStream inputStream = resource.open();
			NativeImage image = NativeImage.read(inputStream);

			List<T> list = new ArrayList<>();

			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int color = /*? if <=1.21.1 {*/ /*fromABGR(image.getPixelRGBA(x, y)); *//*?} else {*/ image.getPixel(x, y); /*?}*/
					if (Boolean.FALSE.equals(filter.getFilter().accept(x, y, width, height, color))) {
						continue;
					}
					T object = texture2Object.accept(x, y, width, height, color);
					if (object != null) {
						list.add(object);
					}
				}
			}

			return list;
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to load create {} from texture \"{}\"! Reason:", id, e);
		}
		return List.of();
	}

	public static int toABGR(int color) {
		return color & -16711936 | (color & 16711680) >> 16 | (color & 255) << 16;
	}

	public static int fromABGR(int color) {
		return toABGR(color);
	}

}
