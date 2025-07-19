package net.lopymine.ip.texture;

import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class RandomStaticParticleTextureProvider extends AbstractParticleTextureProvider {

	@Nullable
	private Identifier currentTexture;

	public RandomStaticParticleTextureProvider(List<Identifier> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected Identifier getInitializationTextureFromNotEmptyTextures(Random random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(random.nextBetween(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected Identifier getTextureFromNotEmptyTextures(Random random) {
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}
}
