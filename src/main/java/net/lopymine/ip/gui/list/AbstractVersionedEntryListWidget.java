package net.lopymine.ip.gui.list;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.widget.ElementListWidget.Entry;

@Getter
public abstract class AbstractVersionedEntryListWidget<E extends Entry<E>> extends ElementListWidget<E> {

	//? if <1.21 {
	/*public boolean visible = true;
	*///?}

	public AbstractVersionedEntryListWidget(int x, int y, int width, int height, int itemHeight) {
		super(MinecraftClient.getInstance(), width, height, y /*? if <1.21 {*//*, y + height *//*?}*/, itemHeight);
		this.setX(x);
		//? if <1.21 {
		/*this.setRenderBackground(false);
		this.setRenderHorizontalShadows(false);
		*///?}
	}

	//? if >=1.21.9 {
	@Override
	protected void drawSelectionHighlight(DrawContext context, E entry, int color) {
	}
	//?} else {
	/*@Override
	protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
	}
	*///?}

	//? if >=1.21 {
	@Override
	protected void drawHeaderAndFooterSeparators(DrawContext context) {
	}
	//?}

	//? if <1.21 {
	/*@Override
	protected void renderBackground(DrawContext context) {
		this.drawMenuListBackground(context);
	}

	protected void drawMenuListBackground(DrawContext context) {

	}
	*///?}

	//? if <1.21 {
	/*@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (!this.visible) {
			return;
		}
		super.render(context, mouseX, mouseY, delta);
	}
	*///?}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
		this.startScissor(context);
		super.renderList(context, mouseX, mouseY, delta);
		this.endScissor(context);
	}

	protected void endScissor(DrawContext context) {
		context.disableScissor();
	}

	protected void startScissor(DrawContext context) {
		context.enableScissor(0, this.getY() + 2, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 2);
	}

	public void setListScrollAmount(int i) {
		//? >=1.21.4 {
		this.setScrollY(i);
		//?} else {
		/*this.setScrollAmount(i);
		*///?}
	}

	//? if <=1.20.1 {
	/*public int getX() {
		return this.left;
	}

	public int getY() {
		return this.top;
	}

	public void setX(int x) {
		this.left = x;
		this.right = this.left + this.getWidth();
	}

	public void setY(int y) {
		this.top = y;
		this.bottom = this.top + this.getHeight();
	}

	public void setPosition(int x, int y) {
		this.setX(x);
		this.setY(y);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getBottom() {
		return this.getY() + this.getHeight();
	}

	public int getRight() {
		return this.getX() + this.getWidth();
	}

	*///?}
}
