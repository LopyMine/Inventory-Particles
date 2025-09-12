package net.lopymine.ip.texture;

import java.util.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class LoopParticleTextureProvider extends StretchParticleTextureProvider {

	public LoopParticleTextureProvider(List<Sprite> textures, double animationSpeed, int lifeTime) {
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
