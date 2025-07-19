package net.lopymine.ip.texture;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractParticleTextureProvider extends TickElement implements IParticleTextureProvider {

	@HideInDebugRender
	public static final Identifier ID = InventoryParticles.id("textures/missing_texture.png");

	protected List<Identifier> textures;
	protected float animationSpeed;
	protected int lifeTime;
	protected boolean shouldDead;

	public AbstractParticleTextureProvider(List<Identifier> textures, float animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	@Override
	public Identifier getInitializationTexture(Random random) {
		if (this.textures.isEmpty()) {
			return ID;
		}
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}

	protected abstract Identifier getInitializationTextureFromNotEmptyTextures(Random random);

	@Override
	public Identifier getTexture(Random random) {
		if (this.textures.isEmpty()) {
			return ID;
		}
		return this.getTextureFromNotEmptyTextures(random);
	}

	protected abstract Identifier getTextureFromNotEmptyTextures(Random random);

	protected void markDead() {
		this.shouldDead = true;
	}
}
