package net.lopymine.ip.element;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.*;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.particle.ParticlePhysics.*;
import net.lopymine.ip.controller.speed.*;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ip.texture.IParticleTextureProvider;
import net.lopymine.ip.utils.DrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.*;

@Setter
@Getter
public class InventoryParticle extends TickElement implements ISelectableElement, IHoverableElement, IMovableElement, IRotatableElement, IRepaintable {

	@HideInDebugRender
	private final Random random = Random.create();

	private final int lifeTimeTicks;
	private final float standardAngle;
	private final IParticleTextureProvider textureProvider;

	private final SpeedController xSpeedController;
	private final SpeedController ySpeedController;
	private final RotationSpeedController<InventoryParticle> rotationSpeedController;
	@Nullable
	private final SpeedInAngleDirectionController<InventoryParticle> speedInAngleDirectionController;

	@NotNull
	private Identifier texture;
	private int color = -1;

	private float lastX;
	private float lastY;
	private float x;
	private float y;

	private float speedX;
	private float speedY;

	private float lastAngle;
	private float angle;

	private boolean dead;
	private boolean selected;
	private boolean hovered;

	public InventoryParticle(ParticleConfig config, InventoryCursor cursor) {
		this.lifeTimeTicks   = config.getLifeTimeTicks();
		this.textureProvider = IParticleTextureProvider.getTextureProvider(config);
		this.texture         = this.textureProvider.getTexture(this.random);

		this.x = cursor.getX() - 4F;
		this.y = cursor.getY() - 4F;

		ParticlePhysics physics = config.getPhysics();

		BasePhysics base = physics.getBase();
		this.xSpeedController = new SpeedController(base.getXSpeed(), this.random, cursor.getSpeedX());
		this.ySpeedController = new SpeedController(base.getYSpeed(), this.random, cursor.getSpeedY());

		RotationSpeedPhysics rotation = physics.getRotation();
		this.rotationSpeedController = new RotationSpeedController<>(rotation.getRotationSpeed(), this.random, rotation.isRotateInMovementDirection());

		AnglePhysics angle = physics.getAngle();
		this.standardAngle                   = angle.getSpawnAngle().getRandom(this.random);
		this.speedInAngleDirectionController = angle.isSpeedInAngleDirectionEnabled() ? new SpeedInAngleDirectionController<>(angle.getAngleSpeedConfig(), this.random, this) : null;
	}

	public void tick() {
		if (this.isDead()) {
			return;
		}

		super.tick();
		this.textureProvider.tick();
		this.texture = this.textureProvider.getTexture(this.random);
		if (this.textureProvider.isShouldDead() || this.ticks > this.getLifeTimeTicks()) {
			if (!this.isSelected()) {
				this.dead = true;
				return;
			}
		}

		this.xSpeedController.tick(this);
		this.speedX = this.xSpeedController.getSpeed();

		this.ySpeedController.tick(this);
		this.speedY = this.ySpeedController.getSpeed();

		this.rotationSpeedController.tick(this);
		this.angle = this.rotationSpeedController.isRotateInMovementDirection() ?
				this.rotationSpeedController.getRotation()
				:
				(this.angle + this.rotationSpeedController.getSpeed()) % 360F;

		if (this.speedInAngleDirectionController != null) {
			this.speedInAngleDirectionController.tick(this);
		}

		this.lastX = this.x;
		this.x += this.speedX;
		this.lastY = this.y;
		this.y += this.speedY;

		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen != null && (this.x > currentScreen.width + 20 || this.y > currentScreen.height + 20)) {
			if (!this.isSelected()) {
				this.dead = true;
			}
		}
	}

	public void render(DrawContext context, InventoryCursor cursor, float tickProgress) {
		InventoryParticlesRenderer renderer = InventoryParticlesRenderer.getInstance();

		float x = renderer.isStopTicking() ? this.x : MathHelper.lerp(tickProgress, this.lastX, this.x);
		float y = renderer.isStopTicking() ? this.y : MathHelper.lerp(tickProgress, this.lastY, this.y);

		this.updateHovered(cursor, (int) x, (int) y, 8, 8);

		boolean bl = (renderer.isStopTicking() && this.isHovered()) || this.isSelected();

		int m = bl ? 4 : 1;

		MatrixStack matrices = context.getMatrices();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		matrices.push();
		int size = 8 * m;
		float halfSize = size / 2F;
		matrices.translate(x, y, 500F);
		matrices.translate(halfSize, halfSize, 0F);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.standardAngle + this.angle));
		matrices.translate(-halfSize, -halfSize, 0F);
		if (bl) {
			matrices.translate(-halfSize, -halfSize, 0F);
		}
		DrawUtils.drawTexture(context, this.texture, 0, 0, 0, 0, size, size, size, size, this.color);
		matrices.pop();
		RenderSystem.disableBlend();
		RenderSystem.disableDepthTest();
	}

	public float getAngle() {
		return this.angle + this.standardAngle;
	}
}
