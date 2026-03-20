package net.lopymine.ip.element.mod;

import lombok.*;
import net.lopymine.ip.element.base.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class InventoryCursor extends TickElement implements IMovableElement {

	private ItemStack currentStack = ItemStack.EMPTY;
	@Nullable
	private Slot hoveredSlot = null;

	private double lastSpeed = 0.0D;
	private double speed = 0.0D;

	private double lastSpeedX = 0.0D;
	private double speedX = 0.0D;

	private double lastSpeedY = 0.0D;
	private double speedY = 0.0D;

	private double lastX = 0;
	private double x = 0;

	private double lastY = 0;
	private double y = 0;

	private double mouseX = 0;
	private double mouseY = 0;

	public void setStack(@Nullable ItemStack currentStack) {
		this.currentStack = currentStack == null || currentStack == ItemStack.EMPTY ? ItemStack.EMPTY : currentStack;
	}

	public void setX(double x) {
		this.lastX = this.x;
		this.x = x;
	}

	public void setY(double y) {
		this.lastY = this.y;
		this.y = y;
	}

	public void setSpeed(double speed) {
		this.lastSpeed = this.speed;
		this.speed = speed;
	}

	public void setSpeedX(double speedX) {
		this.lastSpeedX = this.speedX;
		this.speedX = speedX;
	}

	public void setSpeedY(double speedY) {
		this.lastSpeedY = this.speedY;
		this.speedY = speedY;
	}

	public double getDeltaX() {
		return Math.abs(this.x - this.lastX);
	}

	public double getDeltaY() {
		return Math.abs(this.y - this.lastY);
	}

	public double getSpeedX() {
		double rawCursorSpeedX = this.x - this.lastX;
		int directionalX = rawCursorSpeedX < 0 ? -1 : 1;
		return (Math.sqrt(Math.abs(rawCursorSpeedX)) * directionalX);
	}

	public double getSpeedY() {
		double rawCursorSpeedY = this.y - this.lastY;
		int directionalY = rawCursorSpeedY < 0 ? -1 : 1;
		return (Math.sqrt(Math.abs(rawCursorSpeedY)) * directionalY);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.ticks == 1) {
			this.x = this.getMouseX();
			this.y = this.getMouseY();
			this.lastX = this.x;
			this.lastY = this.y;
		} else {
			this.setX(this.getMouseX());
			this.setY(this.getMouseY());
		}

		double rawCursorSpeedX = this.x - this.lastX;
		int directionalX = rawCursorSpeedX < 0 ? -1 : 1;
		this.setSpeedX((Math.sqrt(Math.abs(rawCursorSpeedX)) * directionalX));

		double rawCursorSpeedY = this.y - this.lastY;
		int directionalY = rawCursorSpeedY < 0 ? -1 : 1;
		this.setSpeedY(Math.sqrt(Math.abs(rawCursorSpeedY)) * directionalY);

		double speed = Math.sqrt(Math.pow(this.getDeltaX(), 2) + Math.pow(this.getDeltaY(), 2));
		this.setSpeed(speed);
	}
}
