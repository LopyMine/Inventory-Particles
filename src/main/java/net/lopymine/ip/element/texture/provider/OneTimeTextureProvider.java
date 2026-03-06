package net.lopymine.ip.element.texture.provider;

import java.util.*;
import net.lopymine.ip.element.texture.ITexture;

public class OneTimeTextureProvider extends StretchTextureProvider {

	public OneTimeTextureProvider(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.animationSpeed;
	}

	@Override
	public void updateCurrentTextureId() {
		if (this.currentTextureId < this.textures.size() - 1) {
			this.currentTextureId++;
		} else {
			this.markDead();
		}
	}
}
