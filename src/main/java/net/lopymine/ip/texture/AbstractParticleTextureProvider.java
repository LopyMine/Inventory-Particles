package net.lopymine.ip.texture;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractParticleTextureProvider extends TickElement implements IParticleTextureProvider {

	protected List<TextureAtlasSprite> textures;
	protected double animationSpeed;
	protected int lifeTime;
	protected boolean shouldDead;

	public AbstractParticleTextureProvider(List<TextureAtlasSprite> textures, double animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	@Override
	public TextureAtlasSprite getInitializationTexture(RandomSource random) {
		if (this.textures.isEmpty()) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}

	protected abstract TextureAtlasSprite getInitializationTextureFromNotEmptyTextures(RandomSource random);

	@Override
	public TextureAtlasSprite getTexture(RandomSource random) {
		if (this.textures.isEmpty()) {
			return InventoryParticlesAtlasManager.getInstance().getMissingSprite();
		}
		return this.getTextureFromNotEmptyTextures(random);
	}

	protected abstract TextureAtlasSprite getTextureFromNotEmptyTextures(RandomSource random);

	protected void markDead() {
		this.shouldDead = true;
	}
}
