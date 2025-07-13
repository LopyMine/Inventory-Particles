package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.util.Identifier;

@Setter
@Getter
public abstract class AbstractTextureProviderWithPeriod extends AbstractTextureProvider {

	protected final int textureTicksPeriod;
	protected int changeTextureTick;

	public AbstractTextureProviderWithPeriod(List<Identifier> textures, int animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
		this.textureTicksPeriod = lifeTime / textures.size();
	}

	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.textureTicksPeriod;
	}
}
