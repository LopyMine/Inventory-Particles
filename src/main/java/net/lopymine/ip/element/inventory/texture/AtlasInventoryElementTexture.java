package net.lopymine.ip.element.inventory.texture;

import lombok.*;
import net.lopymine.ip.utils.ParticleDrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class AtlasInventoryElementTexture implements IInventoryElementTexture {

	@Nullable
	private TextureAtlasSprite sprite;

	public AtlasInventoryElementTexture(@NotNull TextureAtlasSprite sprite) {
		this.sprite = sprite;
	}

	@Override
	public void render(GuiGraphics graphics, float x, float y, float width, float height, int color) {
		ParticleDrawUtils.drawParticleSprite(graphics, this.sprite, 0, 0, width, height, color);
	}
}
