package net.lopymine.ip.texture;

import java.util.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.debug.IDebugRenderable;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.inventory.texture.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public interface IInventoryElementTextureProvider extends ITickElement, IDebugRenderable {

	Map<Object, List<IInventoryElementTexture>> CACHED_SPRITES = new HashMap<>();

	static void clear() {
		CACHED_SPRITES.clear();
	}

	static IInventoryElementTextureProvider getTextureProvider(Object cacheKey, List<Identifier> textures, double animationSpeed, int lifeTime, InventoryElementTextureAnimationType type) {
		List<IInventoryElementTexture> sprites = CACHED_SPRITES.computeIfAbsent(cacheKey, (key) -> textures.stream().map((id) -> {
			if (id.getPath().endsWith(".png")) {
				String path = id.getPath();
				String i = id.getNamespace();
				String s = path.substring(0, path.length() - 4);

				//? if >=1.21 {
				Identifier texture = Identifier.fromNamespaceAndPath(i, s);
				//?} else {
				/*Identifier texture = Identifier.tryBuild(i, s);
				 *///?}
				return new AtlasInventoryElementTexture(InventoryParticlesAtlasManager.getInstance().getSprite(texture));
			}
			return new ItemInventoryElementTexture(new CachedItem(id));
		}).toList());
		
		return switch (type) {
			case STRETCH -> new StretchInventoryElementTextureProvider(sprites, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeInventoryElementTextureProvider(sprites, animationSpeed, lifeTime);
			case LOOP -> new LoopInventoryElementTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM -> new RandomInventoryElementTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticInventoryElementTextureProvider(sprites, animationSpeed, lifeTime);
		};
	}

	IInventoryElementTexture getInitializationTexture(RandomSource random);

	IInventoryElementTexture getTexture(RandomSource random);

	boolean isShouldDead();
}
