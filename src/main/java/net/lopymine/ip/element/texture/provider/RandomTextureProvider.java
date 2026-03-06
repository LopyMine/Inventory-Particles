package net.lopymine.ip.element.texture.provider;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.texture.ITexture;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomTextureProvider extends AbstractTextureProviderWithPeriod {

	@Nullable
	private ITexture currentTexture;

	public RandomTextureProvider(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected ITexture getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected ITexture getTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture != null && this.ticks < this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
	}
}
