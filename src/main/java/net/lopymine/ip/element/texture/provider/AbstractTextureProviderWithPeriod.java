package net.lopymine.ip.element.texture.provider;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.texture.ITexture;

@Setter
@Getter
public abstract class AbstractTextureProviderWithPeriod extends AbstractTextureProvider {

	protected int changeTextureTickPeriod;
	protected double changeTextureTick;

	public AbstractTextureProviderWithPeriod(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
		this.changeTextureTickPeriod = !textures.isEmpty() ? lifeTime / textures.size() : 1;
		this.updateChangeTextureTick();
	}

	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.changeTextureTickPeriod;
	}
}
