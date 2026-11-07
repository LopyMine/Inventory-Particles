package net.lopymine.ip.element.texture;

import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.utils.ItemStackUtils;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

@Getter
@Setter
@ExtensionMethod(DrawContextExtension.class)
public class ItemTexture implements ITexture {

	private static final float ITEM_SIZE = 16F;

	private CachedItem cachedItem;

	@Nullable
	private transient ItemStack stack;

	public ItemTexture(@NotNull CachedItem cachedItem) {
		this.cachedItem = cachedItem;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color) {
		if (this.stack == null) {
			return;
		}
		graphics.push();
		graphics.translate(x, y, 0F);
		graphics.scale(width / ITEM_SIZE, height / ITEM_SIZE, 1F);
		graphics.item(this.stack, 0, 0);
		graphics.pop();
	}

	@Override
	public void initialize() {
		this.stack = ItemStackUtils.create(this.cachedItem.getItem());
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
