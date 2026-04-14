package net.lopymine.ip.config.misc;

import com.mojang.serialization.Codec;
import lombok.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
@AllArgsConstructor
public class CachedItem {

	public static final Codec<CachedItem> CODEC = Identifier.CODEC.xmap(CachedItem::new, CachedItem::getId);

	private Identifier id;
	@Nullable
	private Item item;

	public CachedItem(Identifier id) {
		this.id = id;
	}

	public CachedItem(@NotNull Item item) {
		this.id = BuiltInRegistries.ITEM.getKey(item);
		this.item = item;
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
				if (InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled() || MossyLoader.isDevelopmentEnvironment()) {
					InventoryParticlesClient.LOGGER.error("Failed to find item with id \"{}\"", id);
				}
			}
			return this.item;
		}
		return this.item;
	}
}
