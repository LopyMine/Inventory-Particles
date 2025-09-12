package net.lopymine.ip.texture;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractParticleTextureProvider extends TickElement implements IParticleTextureProvider {

	protected List<Sprite> textures;
	protected double animationSpeed;
	protected int lifeTime;
	protected boolean shouldDead;

	public AbstractParticleTextureProvider(List<Sprite> textures, double animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	@Override
	public Sprite getInitializationTexture(Random random) {
		if (this.textures.isEmpty()) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}

	protected abstract Sprite getInitializationTextureFromNotEmptyTextures(Random random);

	@Override
	public Sprite getTexture(Random random) {
		if (this.textures.isEmpty()) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return this.getTextureFromNotEmptyTextures(random);
	}

	protected abstract Sprite getTextureFromNotEmptyTextures(Random random);

	protected void markDead() {
		this.shouldDead = true;
	}
}
