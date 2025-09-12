package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class StretchParticleTextureProvider extends AbstractParticleTextureProviderWithPeriod {

	protected int currentTextureId = -1;
	@Nullable
	private Sprite currentTexture;

	public StretchParticleTextureProvider(List<Sprite> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected Sprite getInitializationTextureFromNotEmptyTextures(Random random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(0);
		}
		return this.currentTexture;
	}

	@Override
	protected Sprite getTextureFromNotEmptyTextures(Random random) {
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
