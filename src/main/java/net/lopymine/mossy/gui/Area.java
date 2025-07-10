package net.lopymine.mossy.gui;

import lombok.*;

@Getter
@SuppressWarnings("all")
@AllArgsConstructor
public class Area {

	private int x;
	private int y;
	private int width;
	private int height;

	public Area() {
	}

	public int getRight() {
		return this.getX() + this.getWidth();
	}

	public int getBottom() {
		return this.getY() + this.getHeight();
	}

	public int getCenterX() {
		return this.getX() + (this.getWidth() / 2);
	}

	public int getCenterY() {
		return this.getY() + (this.getHeight() / 2);
	}

	public Area x(int x) {
		this.x = x;
		return this;
	}

	public Area y(int y) {
		this.y = y;
		return this;
	}

	public Area offsetX(int offset) {
		this.x += offset;
		return this;
	}

	public Area offsetY(int offset) {
		this.y += offset;
		return this;
	}

	public Area offset(int offsetX, int offsetY) {
		this.offsetX(offsetX);
		this.offsetY(offsetY);
		return this;
	}

	public Area pos(int x, int y) {
		this.x(x);
		this.y(y);
		return this;
	}

	public Area centrolizePos(int x, int y, int width, int height) {
		this.x((Math.max(width - this.getWidth(), 0) / 2) + x);
		this.y((Math.max(height - this.getHeight(), 0) / 2) + y);
		return this;
	}

	public Area centrolizeX(int x, int width) {
		this.x((Math.max(width - this.getWidth(), 0) / 2) + x);
		return this;
	}

	public Area centrolizeY(int y, int height) {
		this.y((Math.max(height - this.getHeight(), 0) / 2) + y);
		return this;
	}

	public Area centrolized() {
		this.x(this.getX() + (this.getWidth() / 2));
		this.y(this.getY() + (this.getHeight() / 2));
		return this;
	}

	public Area width(int width) {
		this.width = width;
		return this;
	}

	public Area height(int height) {
		this.height = height;
		return this;
	}

	public Area expandWidth(int expand) {
		this.width += expand;
		return this;
	}

	public Area expandHeight(int expand) {
		this.height += expand;
		return this;
	}

	public Area expand(int expandWidth, int expandHeight) {
		this.expandWidth(expandWidth);
		this.expandHeight(expandHeight);
		return this;
	}

	public Area size(int width, int height) {
		this.width(width);
		this.height(height);
		return this;
	}

	public boolean over(int mouseX, int mouseY) {
		return mouseX >= this.getX() && mouseX <= this.getRight() && mouseY >= this.getY() && mouseY <= this.getBottom();
	}

	public Area copy() {
		return new Area(this.x, this.y, this.width, this.height);
	}
}
