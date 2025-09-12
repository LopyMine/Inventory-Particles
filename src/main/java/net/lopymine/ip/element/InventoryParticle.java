package net.lopymine.ip.element;

import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.particle.ParticlePhysics.*;
import net.lopymine.ip.controller.color.ColorController;
import net.lopymine.ip.controller.modifier.speed.SpeedInAngleDirectionControllerModifier;
import net.lopymine.ip.controller.size.DynamicSizeController;
import net.lopymine.ip.controller.speed.*;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.extension.DrawContextExtension;
import net.lopymine.ip.renderer.*;
import net.lopymine.ip.spawner.context.ParticleSpawnContext;
import net.lopymine.ip.texture.IParticleTextureProvider;
import net.lopymine.ip.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.*;

@Setter
@Getter
@AllArgsConstructor
@ExtensionMethod(DrawContextExtension.class)
public class InventoryParticle extends TickElement implements IParticle, IRotatableElement, IRepaintable, IRandomizable, IResizableElement {

	@HideInDebugRender
	private final Random random = Random.create();

	private int lifeTimeTicks;
	private double standardParticleAngle;
	private double standardTextureAngle;
	private IParticleTextureProvider textureProvider;

	@Nullable
	private ColorController<InventoryParticle> colorController;

	private DynamicSizeController<InventoryParticle> dynamicSizeController;

	private SpeedController<InventoryParticle> xSpeedController;
	private SpeedController<InventoryParticle> ySpeedController;

	private RotationSpeedController<InventoryParticle> particleRotationSpeedController;
	private RotationSpeedController<InventoryParticle> textureRotationSpeedController;

	@NotNull
	private Sprite texture;
	private int color = -1;

	private double lastWidth = StaticParticleSize.STANDARD_SIZE.getWidth();
	private double lastHeight = StaticParticleSize.STANDARD_SIZE.getHeight();

	private double width = StaticParticleSize.STANDARD_SIZE.getWidth();
	private double height = StaticParticleSize.STANDARD_SIZE.getHeight();

	private double lastX;
	private double lastY;
	private double x;
	private double y;

	private double speedX;
	private double speedY;

	private double lastParticleAngle;
	private double particleAngle;

	private double lastTextureAngle;
	private double textureAngle;

	private boolean dead;
	private boolean selected;
	private boolean hovered;

	public InventoryParticle(ParticleConfig config, ParticleSpawnContext context) {
		this.lifeTimeTicks   = config.getLifeTimeTicks();
		this.textureProvider = IParticleTextureProvider.getTextureProvider(config);
		this.texture         = this.textureProvider.getInitializationTexture(this.random);

		this.dynamicSizeController = new DynamicSizeController<>(config.getSize(), this);

		this.x = context.getX();
		this.lastX = this.x;
		this.y = context.getY();
		this.lastY = this.y;

		ParticlePhysics physics = config.getPhysics();
		BasePhysics base = physics.getBase();
		RotationSpeedPhysics rotation = physics.getRotation();

		this.standardParticleAngle = rotation.getParticleRotationConfig().getSpawnAngle().getRandom(this.random);
		this.particleAngle = 0.0F;
		this.standardTextureAngle  = rotation.getTextureRotationConfig().getSpawnAngle().getRandom(this.random);
		this.textureAngle = 0.0F;

		this.xSpeedController = new SpeedController<>(base.getXSpeed(), this.random, context.getImpulseX());
		this.xSpeedController.registerModifier(new SpeedInAngleDirectionControllerModifier<>(base.getAngleSpeed(), this.random, true), true, this);

		this.ySpeedController = new SpeedController<>(base.getYSpeed(), this.random, context.getImpulseY());
		this.ySpeedController.registerModifier(new SpeedInAngleDirectionControllerModifier<>(base.getAngleSpeed(), this.random, false), true, this);

		this.particleRotationSpeedController = new RotationSpeedController<>(rotation.getParticleRotationConfig(), this.random);
		this.textureRotationSpeedController = new RotationSpeedController<>(rotation.getTextureRotationConfig(), this.random);
	}

	public void tick() {
		if (this.isDead()) {
			return;
		}

		super.tick();
		this.textureProvider.tick();
		this.texture = this.textureProvider.getTexture(this.random);
		if (this.textureProvider.isShouldDead() || this.ticks > this.getLifeTimeTicks()) {
			this.dead = true;
			return;
		}

		if (this.colorController != null) {
			this.colorController.tick(this);
		}

		this.dynamicSizeController.tick(this);

		this.particleRotationSpeedController.tick(this);
		this.lastParticleAngle = this.particleAngle;
		if (this.particleRotationSpeedController.isRotateInMovementDirection()) {
			this.particleAngle = this.particleRotationSpeedController.getRotation();
		} else {
			this.particleAngle = (this.particleAngle + this.particleRotationSpeedController.getSpeed()) % 360F;
		}

		this.textureRotationSpeedController.tick(this);
		this.lastTextureAngle = this.textureAngle;
		if (this.textureRotationSpeedController.isRotateInMovementDirection()) {
			this.textureAngle = this.textureRotationSpeedController.getRotation();
		} else {
			this.textureAngle = (this.textureAngle + this.textureRotationSpeedController.getSpeed()) % 360F;
		}

		this.xSpeedController.tick(this);
		this.ySpeedController.tick(this);

		this.speedX = this.xSpeedController.getSpeed();
		this.speedY = this.ySpeedController.getSpeed();

		this.lastX = this.x;
		this.x += this.speedX;
		this.lastY = this.y;
		this.y += this.speedY;

		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen != null) {
			int width = currentScreen.width;
			int height = currentScreen.height;
			double d = width / 4F;
			double v = height / 4F;
			if (this.x < -d || this.y < -v || this.x > width + d || this.y > height + v) {
				this.dead = true;
			}
		}
	}

	public void render(DrawContext context, InventoryCursor cursor, float tickProgress, boolean stoppedTicking) {
		float renderWidth = stoppedTicking ? (float) this.width : (float) MathHelper.lerp(tickProgress, this.lastWidth, this.width);
		float renderHeight = stoppedTicking ? (float) this.height : (float)  MathHelper.lerp(tickProgress, this.lastHeight, this.height);
		float x = stoppedTicking ? (float) this.x : (float) MathHelper.lerp(tickProgress, this.lastX, this.x);
		float y = stoppedTicking ? (float) this.y : (float) MathHelper.lerp(tickProgress, this.lastY, this.y);

		this.updateHovered(cursor, x, y, renderWidth, renderHeight);
		boolean bl = (stoppedTicking && this.isHovered()) || this.isSelected();

		int m = bl ? 2 : 1;
		
		float width = (renderWidth * m);
		float halfWidth = width / 2F;
		float height = (renderHeight * m);
		float halfHeight = height / 2F;

		context.push();
		context.translate(x, y, 300F);

		if (bl) {
			context.translate(-halfWidth / 2F, -halfHeight / 2F, 0F);
		}

		context.translate(halfWidth, halfHeight, 0F);
		context.rotateZ((float) (this.standardTextureAngle + this.textureAngle) % 360F);
		context.translate(-halfWidth , -halfHeight, 0F);
		DrawUtils.drawParticleSprite(context, this.texture, 0, 0, width, height, this.getRenderColor());
		context.pop();
	}

	private int getRenderColor() {
		int alpha = ArgbUtils.getAlpha(this.color);
		int configAlpha = (int) (InventoryParticlesConfig.getInstance().getParticleConfig().getParticleTransparency() * 255F);
		if (alpha <= configAlpha) {
			return this.color;
		}
		return ArgbUtils.getArgb(configAlpha, ArgbUtils.getRed(this.color), ArgbUtils.getGreen(this.color), ArgbUtils.getBlue(this.color));
	}

	public double getAngle() {
		return this.standardParticleAngle + this.particleAngle;
	}

	@Override
	public void setAngle(double degrees) {
		this.particleAngle = degrees;
	}

	@Override
	public void setWidth(double width) {
		this.lastWidth = this.width;
		this.width = width;
	}

	@Override
	public void setHeight(double height) {
		this.lastHeight = this.height;
		this.height = height;
	}
}
