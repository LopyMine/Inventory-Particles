package net.lopymine.ip.family.generation.batch;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public record ItemRenderRequest(Identifier itemId, Item item, @Nullable BucketItem bucket) {

	public boolean isFluid() {
		return this.bucket != null;
	}
}
