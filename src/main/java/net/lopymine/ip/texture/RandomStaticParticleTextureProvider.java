package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomStaticParticleTextureProvider extends AbstractParticleTextureProvider {

	@Nullable
	private Sprite currentTexture;

	public RandomStaticParticleTextureProvider(List<Sprite> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected Sprite getInitializationTextureFromNotEmptyTextures(Random random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(random.nextBetween(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected Sprite getTextureFromNotEmptyTextures(Random random) {
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}
}
