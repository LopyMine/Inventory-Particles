package net.lopymine.ip.element.texture.provider;

import java.util.*;
import net.lopymine.ip.debug.IDebugRenderable;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.texture.*;
import net.minecraft.util.RandomSource;

public interface ITextureProvider extends ITickElement, IDebugRenderable {

	Map<Object, List<ITexture>> CACHED_SPRITES = new HashMap<>();

	static void clear() {
		CACHED_SPRITES.clear();
	}

	static ITextureProvider getTextureProvider(Object cacheKey, List<ITexture> textures, double animationSpeed, int lifeTime, TextureAnimationType type) {
		List<ITexture> sprites = CACHED_SPRITES.computeIfAbsent(cacheKey, (key) -> textures);
		return switch (type) {
			case STRETCH -> new StretchTextureProvider(sprites, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeTextureProvider(sprites, animationSpeed, lifeTime);
			case LOOP -> new LoopTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM -> new RandomTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticTextureProvider(sprites, animationSpeed, lifeTime);
		};
	}

	ITexture getInitializationTexture(RandomSource random);

	ITexture getTexture(RandomSource random);

	boolean isShouldDead();
}
