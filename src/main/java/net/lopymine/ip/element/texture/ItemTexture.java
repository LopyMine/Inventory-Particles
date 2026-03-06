package net.lopymine.ip.element.texture;

import lombok.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class ItemTexture implements ITexture {

	private CachedItem cachedItem;
	private ItemStack stack;

	public ItemTexture(@NotNull CachedItem cachedItem) {
		this.cachedItem = cachedItem;
		this.stack = cachedItem.getItem().getDefaultInstance();
	}

	@Override
	public void render(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		//todo
		graphics.renderItem(this.stack, 0,0);
	}

	@Override
	public Identifier getId() {
		return this.cachedItem.getId();
	}
}
