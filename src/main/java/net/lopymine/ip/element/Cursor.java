package net.lopymine.ip.element;

import lombok.*;
import net.minecraft.item.*;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class Cursor extends TickElement {

	private Item currentItem = Items.AIR;
	private double lastSpeedX;
	private double lastSpeedY;
	private double lastSpeed;
	private int lastX = 0;
	private int lastY = 0;
	private int mouseX = 0;
	private int mouseY = 0;
	private int x = 0;
	private int y = 0;
	private double speed;
	private double speedX;
	private double speedY;

	public void setCurrentItem(@Nullable Item currentItem) {
		this.currentItem = currentItem == null ? Items.AIR : currentItem;
	}

	public void setX(int x) {
		this.lastX = this.x;
		this.x = x;
	}

	public void setY(int y) {
		this.lastY = this.y;
		this.y = y;
	}

	public void setSpeed(double speed) {
		this.lastSpeed = this.speed;
		this.speed = speed;
	}

	public int getDeltaX() {
		return Math.abs(this.x - this.lastX);
	}

	public int getDeltaY() {
		return Math.abs(this.y - this.lastY);
	}

	public double getDeltaSpeed() {
		return Math.abs(this.speed - this.lastSpeed);
	}

	public double getDeltaSpeedX() {
		return Math.abs(this.speedX - this.lastSpeedX);
	}

	public double getDeltaSpeedY() {
		return Math.abs(this.speedY - this.lastSpeedY);
	}

	@Override
	public void tick() {
		super.tick();
		this.setX(this.getMouseX());
		this.setY(this.getMouseY());
		this.lastSpeedX = this.speedX;
		this.speedX = this.getDeltaX();
		this.lastSpeedY = this.speedY;
		this.speedY = this.getDeltaY();
		this.lastSpeed = this.speed;
		this.speed = Math.sqrt(Math.pow(this.getDeltaX(), 2) + Math.pow(this.getDeltaY(), 2));
	}

	public boolean isHovered(float particleX, float particleY) {
		return this.mouseX > particleX && this.mouseX < particleX + 8 && this.mouseY > particleY && this.mouseY < particleY + 8;
	}
}
