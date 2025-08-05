package net.lopymine.ip.element;

import lombok.*;
import net.lopymine.ip.element.base.TickElement;
import net.minecraft.item.*;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class InventoryCursor extends TickElement {

	private ItemStack currentStack = Items.AIR.getDefaultStack();

	private double lastSpeed = 0.0D;
	private double speed = 0.0D;

	private int lastX = 0;
	private int x = 0;

	private int lastY = 0;
	private int y = 0;

	private int mouseX = 0;
	private int mouseY = 0;

	public void setCurrentStack(@Nullable ItemStack currentStack) {
		this.currentStack = currentStack == null ? Items.AIR.getDefaultStack() : currentStack;
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

	public float getSpeedX() {
		float rawCursorSpeedX = this.x - this.lastX;
		int directionalX = rawCursorSpeedX < 0 ? -1 : 1;
		return (float) (Math.sqrt(Math.abs(rawCursorSpeedX)) * directionalX);
	}

	public float getSpeedY() {
		float rawCursorSpeedY = this.y - this.lastY;
		int directionalY = rawCursorSpeedY < 0 ? -1 : 1;
		return (float) (Math.sqrt(Math.abs(rawCursorSpeedY)) * directionalY);
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
		double speed = Math.sqrt(Math.pow(this.getDeltaX(), 2) + Math.pow(this.getDeltaY(), 2));
		this.setSpeed(speed);
	}
}
