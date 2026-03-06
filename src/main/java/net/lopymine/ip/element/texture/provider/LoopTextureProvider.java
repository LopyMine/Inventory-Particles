package net.lopymine.ip.element.texture.provider;

import java.util.*;
import net.lopymine.ip.element.texture.ITexture;

public class LoopTextureProvider extends StretchTextureProvider {

	public LoopTextureProvider(List<ITexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.animationSpeed;
	}

	@Override
	public void updateCurrentTextureId() {
		this.currentTextureId++;
		if (this.currentTextureId >= this.textures.size()) {
			this.currentTextureId = 0;
		}
	}
}
