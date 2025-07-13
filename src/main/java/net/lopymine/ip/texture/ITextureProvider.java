package net.lopymine.ip.texture;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.lopymine.ip.config.element.InvParticleConfig;
import net.lopymine.ip.element.ITickElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public interface ITextureProvider extends ITickElement {

	static ITextureProvider getTextureProvider(InvParticleConfig config) {
		ArrayList<Identifier> textures = config.getTextures();
		int animationSpeed = config.getAnimationSpeed();
		int lifeTime = config.getLifeTimeTicks();
		return switch (config.getAnimationType()) {
			case STRETCH -> new StretchTextureProvider(textures, animationSpeed, lifeTime);
			case ONETIME -> new OneTimeTextureProvider(textures, animationSpeed, lifeTime);
			case LOOP -> new LoopTextureProvider(textures, animationSpeed, lifeTime);
			case RANDOM -> new RandomTextureProvider(textures, animationSpeed, lifeTime);
			case RANDOM_STATIC -> new RandomStaticTextureProvider(textures, animationSpeed, lifeTime);
		};
	}

	Identifier getTexture(Random random);

	boolean isDead();

	void debugRender(BiConsumer<String, Object> renderLabel);
}
