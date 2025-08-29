package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@Setter
@Getter
public abstract class AbstractParticleTextureProviderWithPeriod extends AbstractParticleTextureProvider {

	protected final int changeTextureTickPeriod;
	protected float changeTextureTick;

	public AbstractParticleTextureProviderWithPeriod(List<Sprite> textures, float animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
		this.changeTextureTickPeriod = !textures.isEmpty() ? lifeTime / textures.size() : 1;
		this.updateChangeTextureTick();
	}

	protected void updateChangeTextureTick() {
		this.changeTextureTick = this.ticks + this.changeTextureTickPeriod;
	}
}
