package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomParticleTextureProvider extends AbstractParticleTextureProviderWithPeriod {

	@Nullable
	private TextureAtlasSprite currentTexture;

	public RandomParticleTextureProvider(List<TextureAtlasSprite> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected TextureAtlasSprite getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected TextureAtlasSprite getTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture != null && this.ticks < this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
	}
}
