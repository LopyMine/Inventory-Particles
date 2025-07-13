package net.lopymine.ip.texture;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class StretchTextureProvider extends AbstractTextureProviderWithPeriod {

	protected int currentTextureId = -1;
	@Nullable
	private Identifier currentTexture;

	public StretchTextureProvider(List<Identifier> textures, int animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	public Identifier getTexture(Random random) {
		if (this.currentTexture != null && this.ticks <= this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateCurrentTextureId();
		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(this.currentTextureId);
	}

	public void updateCurrentTextureId() {
		if (this.currentTextureId < this.textures.size()) {
			this.currentTextureId++;
		}
	}

	@Override
	public void debugRender(BiConsumer<String, Object> renderLabel) {
		renderLabel.accept("Stretch Texture Provider", "See Below");
		renderLabel.accept("Current Texture ID", this.currentTextureId);
		renderLabel.accept("Current Texture", this.currentTexture);
	}
}
