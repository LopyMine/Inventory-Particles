package net.lopymine.ip.element.texture;

import java.util.function.Function;
import lombok.*;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

@Getter
@Setter
@AllArgsConstructor
public class DirectTexture implements ITexture {

	private Identifier location;
	private int textureWidth;
	private int textureHeight;
	private Function<Integer, Integer> color;

	@Override
	public void initialize() {

	}

	@Override
	public void render(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color) {
		//DrawUtils.drawTexture(graphics, this.location, (int) x, (int) y, 0, 0, (int) width, (int) height, this.textureWidth, this.textureHeight);
		graphics.blit(RenderPipelines.GUI_TEXTURED, this.location, (int) x, (int) y, 0, 0, (int) width, (int) height, this.textureWidth, this.textureHeight, this.color.apply(color));
	}

	@Override
	public Identifier getId() {
		return this.location;
	}

	@Override
	public void clear() {

	}
}
