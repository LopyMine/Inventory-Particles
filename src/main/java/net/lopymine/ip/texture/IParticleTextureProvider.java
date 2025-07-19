package net.lopymine.ip.texture;

import java.util.ArrayList;
import net.lopymine.ip.config.particle.ParticleConfig;
import net.lopymine.ip.debug.IDebugRenderable;
import net.lopymine.ip.element.base.ITickElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public interface IParticleTextureProvider extends ITickElement, IDebugRenderable {

	static IParticleTextureProvider getTextureProvider(ParticleConfig config) {
		ArrayList<Identifier> textures = config.getTextures();
		float animationSpeed = config.getAnimationSpeed();
		int lifeTime = config.getLifeTimeTicks();
		return switch (config.getAnimationType()) {
			case STRETCH -> new StretchParticleTextureProvider(textures, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeParticleTextureProvider(textures, animationSpeed, lifeTime);
			case LOOP -> new LoopParticleTextureProvider(textures, animationSpeed, lifeTime);
			case RANDOM -> new RandomParticleTextureProvider(textures, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticParticleTextureProvider(textures, animationSpeed, lifeTime);
		};
	}

	Identifier getInitializationTexture(Random random);

	Identifier getTexture(Random random);

	boolean isShouldDead();
}
