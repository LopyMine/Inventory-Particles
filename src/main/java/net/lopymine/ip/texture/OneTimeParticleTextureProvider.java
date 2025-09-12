package net.lopymine.ip.texture;

import java.util.*;
import net.minecraft.client.texture.Sprite;

public class OneTimeParticleTextureProvider extends StretchParticleTextureProvider {

	public OneTimeParticleTextureProvider(List<Sprite> textures, double animationSpeed, int lifeTime) {
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
