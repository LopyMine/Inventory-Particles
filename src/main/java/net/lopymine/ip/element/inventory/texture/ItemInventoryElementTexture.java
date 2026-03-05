package net.lopymine.ip.element.inventory.texture;

import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class ItemInventoryElementTexture implements IInventoryElementTexture {

	private ItemStack stack;

	public ItemInventoryElementTexture(@NotNull CachedItem cachedItem) {
		this.stack = cachedItem.getItem().getDefaultInstance();
	}

	@Override
	public void render(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		graphics.renderItem(this.stack, 0,0);
	}
}
