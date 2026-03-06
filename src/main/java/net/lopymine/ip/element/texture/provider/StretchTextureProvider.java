package net.lopymine.ip.element.texture.provider;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.texture.ITexture;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class StretchTextureProvider extends AbstractTextureProviderWithPeriod {

	protected int currentTextureId = -1;
	@Nullable
	private ITexture currentTexture;

	public StretchTextureProvider(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected ITexture getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(0);
		}
		return this.currentTexture;
	}

	@Override
	protected ITexture getTextureFromNotEmptyTextures(RandomSource random) {
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
