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

	@Nullable
	private ItemStack stack;

	public ItemTexture(@NotNull CachedItem cachedItem) {
		this.cachedItem = cachedItem;
	}

	@Override
	public void render(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		if (this.stack == null) {
			return;
		}
		graphics.renderItem(this.stack, 0,0);
	}

	@Override
	public void initialize() {
		this.stack = this.cachedItem.getItem().getDefaultInstance();
	}

	@Override
	public void clear() {
		this.stack = null;
	}

	@Override
	public Identifier getId() {
		return this.cachedItem.getId();
	}
}
