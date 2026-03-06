package net.lopymine.ip.element.texture.provider;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.texture.ITexture;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomStaticTextureProvider extends AbstractTextureProvider {

	@Nullable
	private ITexture currentTexture;

	public RandomStaticTextureProvider(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected ITexture getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected ITexture getTextureFromNotEmptyTextures(RandomSource random) {
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}
}
