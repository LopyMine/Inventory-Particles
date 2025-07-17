package net.lopymine.ip.texture;

import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.util.Identifier;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractParticleTextureProvider extends TickElement implements IParticleTextureProvider {

	protected List<Identifier> textures;
	protected float animationSpeed;
	protected int lifeTime;
	protected boolean shouldDead;

	public AbstractParticleTextureProvider(List<Identifier> textures, float animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	protected void markDead() {
		this.shouldDead = true;
	}
}
