package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class StretchParticleTextureProvider extends AbstractParticleTextureProviderWithPeriod {

	protected int currentTextureId = -1;
	@Nullable
	private TextureAtlasSprite currentTexture;

	public StretchParticleTextureProvider(List<TextureAtlasSprite> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected TextureAtlasSprite getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(0);
		}
		return this.currentTexture;
	}

	@Override
	protected TextureAtlasSprite getTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture != null && this.ticks < this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateCurrentTextureId();
		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(this.currentTextureId);
	}

	public void updateCurrentTextureId() {
		if (this.currentTextureId < this.textures.size() - 1) {
			this.currentTextureId++;
		}
	}
}
