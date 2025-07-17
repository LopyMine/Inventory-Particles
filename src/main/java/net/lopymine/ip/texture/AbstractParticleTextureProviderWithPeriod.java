package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.util.Identifier;

@Setter
@Getter
public abstract class AbstractParticleTextureProviderWithPeriod extends AbstractParticleTextureProvider {

	protected final int textureTicksPeriod;
	protected float changeTextureTick;

	public AbstractParticleTextureProviderWithPeriod(List<Identifier> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
		this.textureTicksPeriod = lifeTime / textures.size();
	}

	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.textureTicksPeriod;
	}
}
