package net.lopymine.ip.texture;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomTextureProvider extends AbstractTextureProviderWithPeriod {

	@Nullable
	private Identifier currentTexture;

	public RandomTextureProvider(List<Identifier> textures, int animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	public Identifier getTexture(Random random) {
		if (this.currentTexture != null && this.ticks <= this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateChangeTextureTick();
		return this.currentTexture = this.textures.get(random.nextBetween(0, this.textures.size() - 1));
	}

	@Override
	public void debugRender(BiConsumer<String, Object> renderLabel) {
		renderLabel.accept("Random Texture Provider", "See Below");
		renderLabel.accept("Current Texture", this.currentTexture);
	}
}
