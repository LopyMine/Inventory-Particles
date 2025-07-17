package net.lopymine.ip.texture;

import java.util.*;
import net.minecraft.util.Identifier;

public class LoopParticleTextureProvider extends StretchParticleTextureProvider {

	public LoopParticleTextureProvider(List<Identifier> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	public void updateCurrentTextureId() {
		this.currentTextureId++;
		if (this.currentTextureId >= this.textures.size()) {
			this.currentTextureId = 0;
		}
	}
}
