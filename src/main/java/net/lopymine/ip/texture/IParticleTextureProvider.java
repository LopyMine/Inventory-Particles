package net.lopymine.ip.texture;

import java.util.*;
import net.lopymine.ip.atlas.InventoryParticlesAtlasManager;
import net.lopymine.ip.config.particle.ParticleConfig;
import net.lopymine.ip.debug.IDebugRenderable;
import net.lopymine.ip.element.base.ITickElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public interface IParticleTextureProvider extends ITickElement, IDebugRenderable {

	Map<ParticleConfig, List<Sprite>> CACHED_SPRITES = new HashMap<>();

	static void clear() {
		CACHED_SPRITES.clear();
	}

	static IParticleTextureProvider getTextureProvider(ParticleConfig config) {
		List<Sprite> sprites = CACHED_SPRITES.computeIfAbsent(config, (cfg) -> {
			ArrayList<Identifier> textures = config.getTextures();
			return textures.stream().map(InventoryParticlesAtlasManager.getInstance()::getSprite).toList();
		});

		float animationSpeed = config.getAnimationSpeed();
		int lifeTime = config.getLifeTimeTicks();
		return switch (config.getAnimationType()) {
			case STRETCH -> new StretchParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case LOOP -> new LoopParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM -> new RandomParticleTextureProvider(sprites, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticParticleTextureProvider(sprites, animationSpeed, lifeTime);
		};
	}

	Sprite getInitializationTexture(Random random);

	Sprite getTexture(Random random);

	boolean isShouldDead();
}
