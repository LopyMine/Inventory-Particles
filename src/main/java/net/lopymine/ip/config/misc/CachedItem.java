package net.lopymine.ip.config.misc;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
@AllArgsConstructor
public class CachedItem {

	public static final Codec<CachedItem> CODEC = ResourceLocation.CODEC.xmap(CachedItem::new, CachedItem::getId);

	private ResourceLocation id;
	@Nullable
	private Item item;

	public CachedItem(ResourceLocation id) {
		this.id = id;
	}

	public CachedItem() {
		this.id = BuiltInRegistries.ITEM.getKey(Items.AIR);
	}

	public @NotNull Item getItem() {
		if (this.item == null) {
			//? if >=1.21.4 {
			this.item = BuiltInRegistries.ITEM.getValue(this.id);
			//?} else {
			/*this.item = BuiltInRegistries.ITEM.get(this.id);
			*///?}
			if (this.item == Items.AIR && !this.id.toString().equals("minecraft:air")) {
				InventoryParticlesClient.LOGGER.error("Failed to find item with id \"{}\"", id);
			}
			return this.item;
		}
		return this.item;
	}
}
