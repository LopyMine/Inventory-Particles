package net.lopymine.ip.element.inventory;

import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.particle.StaticParticleSize;
import net.lopymine.ip.controller.IController;
import net.lopymine.ip.controller.color.ColorController;
import net.lopymine.ip.controller.size.DynamicSizeController;
import net.lopymine.ip.controller.speed.*;
import net.lopymine.ip.debug.HideInDebugRender;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.element.base.*;
import net.lopymine.ip.element.inventory.texture.IInventoryElementTexture;
import net.lopymine.ip.texture.IInventoryElementTextureProvider;
import net.lopymine.ip.utils.ArgbUtils2;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.ArgbUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

@Setter
@Getter
@AllArgsConstructor
@ExtensionMethod(DrawContextExtension.class)
public abstract class AbstractInventoryElement<E extends AbstractInventoryElement<E>> extends TickElement implements IInventoryElement, IRotatableElement, IRepaintable, IRandomizable, IResizableElement {

	@HideInDebugRender
	private final RandomSource random = RandomSource.create();
	protected boolean initialized = false;
	private int lifeTimeTicks;
	private double standardParticleAngle;
	private double standardTextureAngle;
	private IInventoryElementTextureProvider textureProvider;
	@Nullable
	private ColorController<E> colorController;
	private DynamicSizeController<E> dynamicSizeController;
	private SpeedController<E> xSpeedController;
	private SpeedController<E> ySpeedController;
	private RotationSpeedController<E> movementRotationSpeedController;
	private RotationSpeedController<E> textureRotationSpeedController;
	private IController<E>[] controllers;
	@NotNull
	private IInventoryElementTexture elementTexture;
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

	public AbstractInventoryElement() {
	}

	protected abstract E getElement();

	public void tick() {
		if (!this.isInitialized()) {
			return;
		}

		if (this.isDead()) {
			return;
		}

		super.tick();
		this.textureProvider.tick();
		this.elementTexture = this.textureProvider.getTexture(this.random);
		if (this.textureProvider.isShouldDead() || this.ticks > this.getLifeTimeTicks()) {
			this.dead = true;
			return;
		}

		if (this.colorController != null) {
			this.colorController.tick(this.getElement());
		}

		this.dynamicSizeController.tick(this.getElement());

		this.movementRotationSpeedController.tick(this.getElement());
		this.lastParticleAngle = this.particleAngle;
		if (this.movementRotationSpeedController.isRotateInMovementDirection()) {
			this.particleAngle = this.movementRotationSpeedController.getRotation();
		} else {
			this.particleAngle = (this.particleAngle + this.movementRotationSpeedController.getSpeed()) % 360F;
		}

		this.textureRotationSpeedController.tick(this.getElement());
		this.lastTextureAngle = this.textureAngle;
		if (this.textureRotationSpeedController.isRotateInMovementDirection()) {
			this.textureAngle = this.textureRotationSpeedController.getRotation();
		} else {
			this.textureAngle = (this.textureAngle + this.textureRotationSpeedController.getSpeed()) % 360F;
		}

		this.xSpeedController.tick(this.getElement());
		this.ySpeedController.tick(this.getElement());

		this.processCustomControllers();

		this.speedX = this.xSpeedController.getSpeed();
		this.speedY = this.ySpeedController.getSpeed();

		this.lastX = this.x;
		this.x += this.speedX;
		this.lastY = this.y;
		this.y += this.speedY;

		this.checkScreenBounds();
	}

	protected void processCustomControllers() {

	}

	protected void checkScreenBounds() {
		if (!this.isInitialized()) {
			return;
		}
		Screen currentScreen = Minecraft.getInstance().screen;
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

	public void render(GuiGraphics context, InventoryCursor cursor, float tickProgress, boolean stoppedTicking) {
		if (!this.isInitialized()) {
			return;
		}
		float renderWidth = stoppedTicking ? (float) this.width : (float) Mth.lerp(tickProgress, this.lastWidth, this.width);
		float renderHeight = stoppedTicking ? (float) this.height : (float) Mth.lerp(tickProgress, this.lastHeight, this.height);
		float x = stoppedTicking ? (float) this.x : (float) Mth.lerp(tickProgress, this.lastX, this.x);
		float y = stoppedTicking ? (float) this.y : (float) Mth.lerp(tickProgress, this.lastY, this.y);

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
		context.translate(-halfWidth, -halfHeight, 0F);
		this.renderElementTexture(context, width, height);
		context.pop();
	}

	protected void renderElementTexture(GuiGraphics context, float width, float height) {
		this.getElementTexture().render(context, 0, 0, width, height, this.getRenderColor());
	}

	@SuppressWarnings("unused")
	protected int getRenderColor() {
		if (!this.isInitialized()) {
			return -1;
		}
		int alpha = ArgbUtils.getAlpha(this.color);
		int configAlpha = (int) (InventoryParticlesConfig.getInstance().getParticleConfig().getParticleTransparency() * 255F);
		if (alpha <= configAlpha) {
			return this.color;
		}
		return ArgbUtils2.getArgb(configAlpha, ArgbUtils2.getRed(this.color), ArgbUtils2.getGreen(this.color), ArgbUtils2.getBlue(this.color));
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
		this.width     = width;
	}

	@Override
	public void setHeight(double height) {
		this.lastHeight = this.height;
		this.height     = height;
	}
}
