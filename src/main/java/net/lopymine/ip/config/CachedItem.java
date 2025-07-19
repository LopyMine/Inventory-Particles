package net.lopymine.ip.config;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class CachedItem {

	public static final Codec<CachedItem> CODEC = Identifier.CODEC.xmap(CachedItem::new, CachedItem::getId);

	private final Identifier id;
	@Nullable
	private Item item;

	public CachedItem(Identifier id) {
		this.id = id;
	}

	public CachedItem() {
		this.id = Registries.ITEM.getId(Items.AIR);
	}

	public @Nullable Item getItem() {
		if (this.item == null) {
			this.item = Registries.ITEM.get(this.id);
			if (this.item == Items.AIR && !this.id.toString().equals("minecraft:air")) {
				InventoryParticlesClient.LOGGER.error("Failed to find item with id \"{}\"", id);
			}
			return this.item;
		}
		return this.item;
	}
}
