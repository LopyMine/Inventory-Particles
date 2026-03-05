package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.inventory.texture.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class StretchInventoryElementTextureProvider extends AbstractInventoryElementTextureProviderWithPeriod {

	protected int currentTextureId = -1;
	@Nullable
	private IInventoryElementTexture currentTexture;

	public StretchInventoryElementTextureProvider(List<IInventoryElementTexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected IInventoryElementTexture getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(0);
		}
		return this.currentTexture;
	}

	@Override
	protected IInventoryElementTexture getTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture != null && this.ticks < this.changeTextureTick) {
			return this.currentTexture;
		}

		this.updateCurrentTextureId();
		this.updateChangeTextureTick();

		return this.currentTexture = this.textures.get(this.currentTextureId);
	}

	public void updateCurrentTextureId() {
		if (this.currentTextureId < this.textures.size() - 1) {
			this.currentTextureId++;
		}
	}
}
