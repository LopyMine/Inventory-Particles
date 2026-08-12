package net.lopymine.ip.family.generation.batch;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import net.lopymine.ip.family.FamilyParticleData.TextureExtractMode;
import net.lopymine.ip.utils.iac.*;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class RenderedItemImages {

	private final Map<Item, RenderedItemImage> items = new IdentityHashMap<>();
	private final Map<Item, RenderedFluidImage> fluids = new IdentityHashMap<>();

	public void putItem(Item item, RenderedItemImage image) {
		this.items.put(item, image);
	}

	public void putFluid(Item item, RenderedFluidImage image) {
		this.fluids.put(item, image);
	}

	@Nullable
	public RenderedItemImage get(Item item, TextureExtractMode textureExtractMode) {
		if (textureExtractMode == TextureExtractMode.FLUID) {
			RenderedFluidImage fluid = this.fluids.get(item);
			if (fluid != null) {
				return fluid;
			}
		}
		return this.items.get(item);
	}

	@Nullable
	public RenderedFluidImage getFluid(Item item) {
		return this.fluids.get(item);
	}

	public boolean isEmpty() {
		return this.items.isEmpty() && this.fluids.isEmpty();
	}

	public void closeAndClear() {
		this.close(this.items);
		this.close(this.fluids);
		this.items.clear();
		this.fluids.clear();
	}

	private <T extends RenderedItemImage> void close(Map<Item, T> map) {
		for (RenderedItemImage image : map.values()) {
			NativeImage nativeImage = image.getImage();
			if (nativeImage != null) {
				nativeImage.close();
			}
		}
	}
}
