package net.lopymine.ip.texture;

import java.util.*;
import lombok.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.inventory.texture.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@AllArgsConstructor
public abstract class AbstractInventoryElementTextureProvider extends TickElement implements IInventoryElementTextureProvider {

	protected List<IInventoryElementTexture> textures;
	protected double animationSpeed;
	protected int lifeTime;
	protected boolean shouldDead;

	@Nullable
	protected IInventoryElementTexture missingTexture;

	public AbstractInventoryElementTextureProvider(List<IInventoryElementTexture> textures, double animationSpeed, int lifeTime, boolean shouldDead) {
		this.textures       = textures;
		this.animationSpeed = animationSpeed;
		this.lifeTime       = lifeTime;
		this.shouldDead     = shouldDead;
	}

	public AbstractInventoryElementTextureProvider(List<IInventoryElementTexture> textures, double animationSpeed, int lifeTime) {
		this(textures, animationSpeed, lifeTime, false);
	}

	@Override
	public IInventoryElementTexture getInitializationTexture(RandomSource random) {
		if (this.textures.isEmpty()) {
			return this.getMissingSprite();
		}
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}

	private IInventoryElementTexture getMissingSprite() {
		if (this.missingTexture == null) {
			this.missingTexture = new AtlasInventoryElementTexture(InventoryParticlesAtlasManager.getInstance().getMissingSprite());
		}
		return this.missingTexture;
	}

	protected abstract IInventoryElementTexture getInitializationTextureFromNotEmptyTextures(RandomSource random);

	@Override
	public IInventoryElementTexture getTexture(RandomSource random) {
		if (this.textures.isEmpty()) {
			return this.getMissingSprite();
		}
		return this.getTextureFromNotEmptyTextures(random);
	}

	protected abstract IInventoryElementTexture getTextureFromNotEmptyTextures(RandomSource random);

	protected void markDead() {
		this.shouldDead = true;
	}
}
