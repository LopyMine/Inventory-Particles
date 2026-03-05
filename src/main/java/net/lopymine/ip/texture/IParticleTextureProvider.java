package net.lopymine.ip.texture;

import java.util.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.debug.IDebugRenderable;
import net.lopymine.ip.element.base.ITickElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public interface IParticleTextureProvider extends ITickElement, IDebugRenderable {

	Map<ParticleConfig, List<TextureAtlasSprite>> CACHED_SPRITES = new HashMap<>();

	static void clear() {
		CACHED_SPRITES.clear();
	}

	static IParticleTextureProvider getTextureProvider(ParticleConfig config) {
		List<TextureAtlasSprite> sprites = CACHED_SPRITES.computeIfAbsent(config, (cfg) -> {
			ArrayList<ParticleTexture> textures = config.getTextures();
			return textures.stream().map((texture) -> InventoryParticlesAtlasManager.getInstance().getSprite(texture.getSprite(), texture.getAtlasId())).toList();
		});

		double animationSpeed = config.getAnimationSpeed();
		int lifeTime = config.getLifeTimeTicks();
		return switch (config.getAnimationType()) {
			case STRETCH -> new StretchParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case LOOP -> new LoopParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM -> new RandomParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticParticleTextureProvider(sprites, animationSpeed, lifeTime);
		};
	}

	TextureAtlasSprite getInitializationTexture(RandomSource random);

	TextureAtlasSprite getTexture(RandomSource random);

	boolean isShouldDead();
}
