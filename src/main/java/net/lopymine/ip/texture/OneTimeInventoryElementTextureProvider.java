package net.lopymine.ip.texture;

import java.util.*;
import net.lopymine.ip.element.inventory.texture.IInventoryElementTexture;

public class OneTimeInventoryElementTextureProvider extends StretchInventoryElementTextureProvider {

	public OneTimeInventoryElementTextureProvider(List<IInventoryElementTexture> textures, double animationSpeed, int lifeTime) {
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
