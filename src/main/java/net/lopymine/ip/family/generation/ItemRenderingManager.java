package net.lopymine.ip.family.generation;

import java.util.concurrent.atomic.AtomicReference;
import net.lopymine.ip.utils.iac.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemRenderingManager {

	@Nullable
	public static RenderedItemImage getRenderedItemImage(Item item) {
		if (Minecraft.getInstance().level == null) {
			return null;
		}

		if (item instanceof BoatItem) {
			return getRenderedFluid((BucketItem) Items.WATER_BUCKET);
		} else if (item instanceof BucketItem bucketItem) {
			return getRenderedFluid(bucketItem);
		} else {
			return getRenderedItem(item);
		}
	}

	private static RenderedItemImage getRenderedItem(Item item) {
		AtomicReference<RenderedItemImage> atomic = new AtomicReference<>();
		ItemRendering.renderItemIntoImage(item.getDefaultInstance(), atomic::set);
		while (atomic.get() == null) {
			Thread.onSpinWait();
		}
		return atomic.get();
	}

	private static RenderedFluidImage getRenderedFluid(BucketItem bucketItem) {
		AtomicReference<RenderedFluidImage> atomic = new AtomicReference<>();
		ItemRendering.renderFluidIntoImage(bucketItem, atomic::set);
		while (atomic.get() == null) {
			Thread.onSpinWait();
		}
		return atomic.get();
	}

}
