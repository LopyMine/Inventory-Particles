package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomParticleTextureProvider extends AbstractParticleTextureProviderWithPeriod {

	@Nullable
	private Identifier currentTexture;

	public RandomParticleTextureProvider(List<Identifier> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected Identifier getInitializationTextureFromNotEmptyTextures(Random random) {
		if (this.currentTexture == null) {
			return this.textures.get(random.nextBetween(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected Identifier getTextureFromNotEmptyTextures(Random random) {
		if (this.currentTexture != null && this.ticks < this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(random.nextBetween(0, this.textures.size() - 1));
	}
}
