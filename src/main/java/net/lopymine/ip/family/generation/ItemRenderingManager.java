package net.lopymine.ip.family.generation;

import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.family.FamilyParticleData.TextureExtractMode;
import net.lopymine.ip.utils.iac.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemRenderingManager {

	@Nullable
	public static RenderedItemImage getRenderedItemImage(Item item, Identifier itemId, TextureExtractMode textureExtractMode) {
		if (Minecraft.getInstance().level == null) {
			return null;
		}

		if (textureExtractMode == TextureExtractMode.FLUID) {
			BucketItem bucketItem;

			if (item instanceof BucketItem bucket) {
				bucketItem = bucket;
			} else if (item instanceof BoatItem) {
				bucketItem = (BucketItem) Items.WATER_BUCKET;
			} else {
				bucketItem = null;
			}

			if (bucketItem != null) {
				return getRenderedFluid(bucketItem, itemId);
			}
		}

		return getRenderedItem(item, itemId);
	}

	@Nullable
	private static RenderedItemImage getRenderedItem(Item item, Identifier itemId) {
		RenderingItemImage<RenderedItemImage> renderingItemImage = new RenderingItemImage<>();
		ItemRendering.renderItemIntoImage(item.getDefaultInstance(), renderingItemImage::setRenderedItemImage);
		int i = 0;
		while (!renderingItemImage.isReady() && i < 3000) {
			try {
				Thread.sleep(10);
				i++;
			} catch (Exception ignored) { }
		}
		if (i >= 3000) {
			InventoryParticles.LOGGER.error("Skipping rendering \"{}\" because it took too long!", itemId);
		}
		return renderingItemImage.getRenderedItemImage();
	}

	@Nullable
	private static RenderedItemImage getRenderedFluid(BucketItem bucketItem, Identifier itemId) {
		RenderingItemImage<RenderedFluidImage> renderingFluidImage = new RenderingItemImage<>();
		ItemRendering.renderFluidIntoImage(bucketItem, renderingFluidImage::setRenderedItemImage);

		int i = 0;
		while (!renderingFluidImage.isReady() && i < 3000) {
			try {
				Thread.sleep(10);
				i++;
			} catch (Exception ignored) { }
		}
		if (i >= 3000) {
			InventoryParticles.LOGGER.error("Skipping rendering \"{}\" because it took too long!", itemId);
		}
		return renderingFluidImage.getRenderedItemImage();
	}

	@Getter
	public static class RenderingItemImage<T extends RenderedItemImage> {

		@Nullable
		private T renderedItemImage;
		private boolean ready;

		public void setRenderedItemImage(@Nullable T renderedItemImage) {
			this.renderedItemImage = renderedItemImage;
			this.ready             = true;
		}
	}





}
