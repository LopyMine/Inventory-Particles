package net.lopymine.ip.element.texture;

import java.util.function.Function;
import lombok.*;
import net.lopymine.ip.renderer.OptimizedDrawer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.*;

@Getter
@Setter
public class ColoredAtlasTexture extends AtlasTexture {

	private Function<Integer, Integer> color;

	public ColoredAtlasTexture(@Nullable TextureAtlasSprite sprite,  Function<Integer, Integer> color) {
		super(sprite);
		this.color = color;
	}

	public ColoredAtlasTexture(@Nullable Identifier sprite, @Nullable Identifier atlas,  Function<Integer, Integer> color) {
		super(sprite, atlas);
		this.color = color;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color) {
		if (this.getAtlasSprite() == null) {
			return;
		}
		OptimizedDrawer.drawParticleSprite(graphics, this.getAtlasSprite(), 0, 0, width, height, this.color.apply(color));
	}

}
