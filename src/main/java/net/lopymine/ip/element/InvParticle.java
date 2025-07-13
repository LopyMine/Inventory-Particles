package net.lopymine.ip.element;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.element.*;
import net.lopymine.ip.config.element.InvParticlePhysics.*;
import net.lopymine.ip.renderer.InvParticlesRenderer;
import net.lopymine.ip.texture.ITextureProvider;
import net.lopymine.ip.utils.DrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class InvParticle extends TickElement {

	private final Random random = Random.create();
	private final ITextureProvider textureProvider;
	private final InvParticleConfig config;
	private final float accelerationX;
	private final float accelerationY;
	private final float accelerationRotation;
	@NotNull
	private Identifier texture;
	private float lastX;
	private float lastY;
	private float x;
	private float y;
	private float speedX;
	private float speedY;
	private float speedRotation;
	private boolean dead;

	public InvParticle(InvParticleConfig config, Cursor cursor) {
		this.config = config;
		this.textureProvider = ITextureProvider.getTextureProvider(config);
		this.texture = this.textureProvider.getTexture(this.random);

		this.x = cursor.getX() - 4F;
		this.y = cursor.getY() - 4F;

		InvParticlePhysics physics = config.getPhysics();
		BasePhysics base = physics.getBase();

		float cursorSpeedX;
		float cursorSpeedY;

		if (base.isCursorImpulseX()) {
			float rawCursorSpeedX = cursor.getLastX() - cursor.getX();
			int directionalX = rawCursorSpeedX < 0 ? -1 : 1;
			cursorSpeedX = (float) (Math.sqrt(Math.abs(rawCursorSpeedX)) * directionalX);
		} else {
			cursorSpeedX = 0F;
		}

		if (base.isCursorImpulseY()) {
			float rawCursorSpeedY = cursor.getLastY() - cursor.getY();
			int directionalY = rawCursorSpeedY < 0 ? -1 : 1;
			cursorSpeedY = (float) (Math.sqrt(Math.abs(rawCursorSpeedY)) * directionalY);
		} else {
			cursorSpeedY = 0F;
		}

		this.speedX = cursorSpeedX + base.getImpulseX().getRandom(this.random) * base.getImpulseXMultiplier(this.random);
		this.speedY = cursorSpeedY + base.getImpulseY().getRandom(this.random) * base.getImpulseYMultiplier(this.random);

		this.accelerationX = base.getAccelerationX() * base.getAccelerationXMultiplier(this.random);
		this.accelerationY = base.getAccelerationY() * base.getAccelerationYMultiplier(this.random);

		RotationPhysics rotation = physics.getRotation();
		this.speedRotation = rotation.getRotationImpulse().getRandom(this.random) * rotation.getRotationImpulseMultiplayer(this.random);

		this.accelerationRotation = rotation.getRotationAcceleration() * rotation.getRotationAccelerationMultiplayer(this.random);
	}

	public void tick() {
		super.tick();
		this.textureProvider.tick();
		this.texture = this.textureProvider.getTexture(this.random);
		if (this.textureProvider.isDead() || this.ticks > this.config.getLifeTimeTicks()) {
			this.dead = true;
		}

		InvParticlePhysics physics = this.config.getPhysics();
		AirPhysics air = physics.getAir();
		RotationPhysics rotation = physics.getRotation();

		if (this.speedX < air.getTerminalVelocityX()) {
			this.speedX = this.speedX + this.accelerationX;
		}
		if (this.speedY < air.getTerminalVelocityY()) {
			this.speedY = this.speedY + this.accelerationY;
		}
		if (this.speedRotation < rotation.getTerminalRotationVelocity()) {
			this.speedRotation = this.speedRotation + this.accelerationRotation;
		}

		if (this.speedX > 0) {
			this.speedX = this.speedX - air.getBrakingX();
		}
		if (this.speedY > 0) {
			this.speedY = this.speedY - air.getBrakingY();
		}
		if (this.speedRotation > 0) {
			this.speedRotation = this.speedRotation - rotation.getRotationBraking();
		}

		this.speedX = this.speedX - air.getTurbulenceX().getRandom(this.random);
		this.speedY = this.speedY - air.getTurbulenceY().getRandom(this.random);

		this.lastX = this.x;
		this.lastY = this.y;
		this.x = (this.x - this.speedX);
		this.y = (this.y - this.speedY);

		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen != null && (this.x > currentScreen.width + 20 || this.y > currentScreen.height + 20)) {
			this.dead = true;
		}
	}

	public void render(DrawContext context, Cursor cursor, float tickProgress) {
		MatrixStack matrices = context.getMatrices();

		InvParticlesRenderer renderer = InvParticlesRenderer.getInstance();

		float x = renderer.isStopTicking() ? this.x : MathHelper.lerp(tickProgress, this.lastX, this.x);
		float y = renderer.isStopTicking() ? this.y : MathHelper.lerp(tickProgress, this.lastY, this.y);

		boolean bl = cursor.isHovered(x, y) && renderer.isStopTicking() || (renderer.getHoveredParticle() == this && renderer.isLockHoveredParticle());

		if (bl) {
			renderer.setHoveredParticle(this);
		}

		int m = bl ? 4 : 1;

		RenderSystem.enableDepthTest();
		matrices.push();
		int size = 8 * m;
		float halfSize = size / 2F;
		matrices.translate(x, y, 500F);
		matrices.translate(halfSize, halfSize, 0F);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.speedRotation));
		matrices.translate(-halfSize, -halfSize, 0F);
		DrawUtils.drawTexture(context, this.texture, 0, 0, 0, 0, size, size, size, size);
		matrices.pop();
		RenderSystem.disableDepthTest();
	}
}
