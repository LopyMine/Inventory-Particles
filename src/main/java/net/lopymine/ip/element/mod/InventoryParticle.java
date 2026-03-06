package net.lopymine.ip.element.mod;

import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.particle.ParticlePhysics.*;
import net.lopymine.ip.element.controller.modifier.speed.SpeedInAngleDirectionControllerModifier;
import net.lopymine.ip.element.controller.size.DynamicSizeController;
import net.lopymine.ip.element.controller.speed.*;
import net.lopymine.ip.element.inventory.AbstractInventoryElement;
import net.lopymine.ip.element.mod.spawner.context.ParticleSpawnContext;
import net.lopymine.ip.element.texture.provider.ITextureProvider;
import net.minecraft.util.RandomSource;

public class InventoryParticle extends AbstractInventoryElement<InventoryParticle> {

	public static InventoryParticle create(ParticleConfig config, ParticleSpawnContext context) {
		InventoryParticle particle = new InventoryParticle();
		RandomSource random = particle.getRandom();

		int lifeTimeTicks = config.getLifeTimeTicks();
		particle.setLifeTimeTicks(lifeTimeTicks);
		ITextureProvider textureProvider = ITextureProvider.getTextureProvider(config, config.getTextures(), config.getAnimationSpeed(), config.getLifeTimeTicks(), config.getAnimationType());
		particle.setTextureProvider(textureProvider);
		particle.setElementTexture(textureProvider.getInitializationTexture(random));

		DynamicSizeController<InventoryParticle> dynamicSizeController = new DynamicSizeController<>(config.getSize(), particle);
		particle.setDynamicSizeController(dynamicSizeController);

		double x = context.getX();
		particle.setX(x);
		particle.setLastX(x);
		double y = context.getY();
		particle.setY(y);
		particle.setLastY(y);

		ParticlePhysics physics = config.getPhysics();
		BasePhysics base = physics.getBase();
		RotationSpeedPhysics rotation = physics.getRotation();

		double standardParticleAngle = rotation.getParticleRotationConfig().getSpawnAngle().getRandom(random);
		particle.setStandardParticleAngle(standardParticleAngle);
		double standardTextureAngle = rotation.getTextureRotationConfig().getSpawnAngle().getRandom(random);
		particle.setStandardTextureAngle(standardTextureAngle);

		SpeedController<InventoryParticle> xSpeedController = new SpeedController<>(base.getXSpeed(), random, context.getImpulseX());
		particle.setXSpeedController(xSpeedController);
		xSpeedController.registerModifier(new SpeedInAngleDirectionControllerModifier<>(base.getAngleSpeed(), random, true), true, particle);

		SpeedController<InventoryParticle> ySpeedController = new SpeedController<>(base.getYSpeed(), random, context.getImpulseY());
		particle.setYSpeedController(ySpeedController);
		ySpeedController.registerModifier(new SpeedInAngleDirectionControllerModifier<>(base.getAngleSpeed(), random, false), true, particle);

		RotationSpeedController<InventoryParticle> particleRotationSpeedController = new RotationSpeedController<>(rotation.getParticleRotationConfig(), random);
		particle.setMovementRotationSpeedController(particleRotationSpeedController);
		RotationSpeedController<InventoryParticle> textureRotationSpeedController = new RotationSpeedController<>(rotation.getTextureRotationConfig(), random);
		particle.setTextureRotationSpeedController(textureRotationSpeedController);

		particle.setInitialized(true);
		return particle;
	}

	@Override
	protected InventoryParticle getElement() {
		return this;
	}
}
