package net.lopymine.ip.family.generation;

import java.util.List;
import net.lopymine.ip.family.FamilyParticleData.TextureExtractMode;
import net.lopymine.ip.family.generation.batch.*;
import net.lopymine.ip.utils.iac.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemRenderingManager {

	@Nullable
	public static BucketItem resolveBucket(Item item, TextureExtractMode textureExtractMode) {
		if (textureExtractMode != TextureExtractMode.FLUID) {
			return null;
		}
		if (item instanceof BucketItem bucketItem) {
			return bucketItem;
		}
		if (item instanceof BoatItem) {
			return (BucketItem) Items.WATER_BUCKET;
		}
		return null;
	}

	@Nullable
	public static RenderedItemImage renderItemImage(Item item, Identifier itemId, TextureExtractMode textureExtractMode, int cellSize) {
		if (Minecraft.getInstance().level == null) {
			return null;
		}

		BucketItem bucketItem = resolveBucket(item, textureExtractMode);
		RenderedItemImages images = ItemRenderBatcher.render(List.of(new ItemRenderRequest(itemId, item, bucketItem)), cellSize);

		return images.get(item, textureExtractMode);
	}

	@Nullable
	public static RenderedItemImage renderItemImageIfSpecial(Identifier itemId, Item item, TextureExtractMode textureExtractMode, int cellSize) {
		if (Minecraft.getInstance().level == null) {
			return null;
		}

		BucketItem bucketItem = resolveBucket(item, textureExtractMode);
		if (bucketItem != null) {
			RenderedItemImages images = ItemRenderBatcher.render(List.of(new ItemRenderRequest(itemId, item, bucketItem)), cellSize);
			return images.getFluid(item);
		}

		return null;
	}
}
