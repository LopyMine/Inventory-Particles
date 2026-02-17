package net.lopymine.ip.texture;

import java.util.List;
import lombok.*;
import net.lopymine.ip.element.inventory.texture.IInventoryElementTexture;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class RandomStaticInventoryElementTextureProvider extends AbstractInventoryElementTextureProvider {

	@Nullable
	private IInventoryElementTexture currentTexture;

	public RandomStaticInventoryElementTextureProvider(List<IInventoryElementTexture> textures, double animationSpeed, int lifeTime) {
		super(textures, animationSpeed, lifeTime);
	}

	@Override
	protected IInventoryElementTexture getInitializationTextureFromNotEmptyTextures(RandomSource random) {
		if (this.currentTexture == null) {
			return this.currentTexture = this.textures.get(random.nextIntBetweenInclusive(0, this.textures.size() - 1));
		}
		return this.currentTexture;
	}

	@Override
	protected IInventoryElementTexture getTextureFromNotEmptyTextures(RandomSource random) {
		return this.getInitializationTextureFromNotEmptyTextures(random);
	}
}
