package net.lopymine.ip.texture;

import java.util.*;
import net.minecraft.util.Identifier;

public class LoopTextureProvider extends StretchTextureProvider {

	public LoopTextureProvider(List<Identifier> textures, int animationSpeed, int lifeTime) {
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
