package net.lopymine.ip.texture;

import java.util.*;
import net.minecraft.util.Identifier;

public class OneTimeTextureProvider extends StretchTextureProvider {

	public OneTimeTextureProvider(List<Identifier> textures, int animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected void updateChangeTextureTick() {
		if (this.currentTextureId < this.textures.size()) {
			this.currentTextureId++;
			if (this.currentTextureId == this.textures.size()) {
				this.markDead();
			}
		}
	}
}
