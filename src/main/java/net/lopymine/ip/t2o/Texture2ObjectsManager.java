package net.lopymine.ip.t2o;

import java.io.InputStream;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class Texture2ObjectsManager {

	public static <T> List<T> readFromTexture(Identifier id, String objectName, Texture2ObjectPixelFilter filter, Texture2Object<T> texture2Object) {
		try {
			Optional<Resource> optional = MinecraftClient.getInstance().getResourceManager().getResource(id);
			if (optional.isEmpty()) {
				InventoryParticlesClient.LOGGER.error("Failed to find texture from \"{}\" to create {} from texture!", id, objectName);
				return List.of();
			}
			Resource resource = optional.get();
			InputStream inputStream = resource.getInputStream();
			NativeImage image = NativeImage.read(inputStream);

			List<T> list = new ArrayList<>();

			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int color = image./*? if <=1.21.1 {*/ getColor /*?} else {*/ /*getColorArgb *//*?}*/(x, y);
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

}
