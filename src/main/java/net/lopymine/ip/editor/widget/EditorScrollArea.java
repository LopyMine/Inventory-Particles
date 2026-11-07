package net.lopymine.ip.editor.widget;

//? if >=1.21.11 {

import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.network.chat.Component;

public abstract class EditorScrollArea extends AbstractScrollArea {

	private final int scrollRate;

	public EditorScrollArea(int x, int y, int width, int height, Component message, int scrollRate) {
		//? if >=26.1 {
		super(x, y, width, height, message, AbstractScrollArea.defaultSettings(scrollRate));
		//?} else {
		/*super(x, y, width, height, message);
		*///?}
		this.scrollRate = scrollRate;
	}

	//? if <26.1 {
	/*public int scrollbarWidth() {
		return SCROLLBAR_WIDTH;
	}
	*///?}

	@Override
	protected double scrollRate() {
		return this.scrollRate;
	}
}

//?} else {

/*import com.mojang.blaze3d.platform.InputConstants;
import net.lopymine.mossylib.gui.TransparencySprites;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class EditorScrollArea extends AbstractWidget {

	public static final int SCROLLBAR_WIDTH = 6;

	private static final int SCROLLER_MIN_HEIGHT = 32;
	private static final int SPRITE_WIDTH = 6;
	private static final int SPRITE_HEIGHT = 32;
	private static final int SPRITE_BORDER = 1;

	private final int scrollRate;

	private double scrollAmount;
	private boolean scrolling;

	public EditorScrollArea(int x, int y, int width, int height, Component message, int scrollRate) {
		super(x, y, width, height, message);
		this.scrollRate = scrollRate;
	}

	protected abstract int contentHeight();

	protected double scrollRate() {
		return this.scrollRate;
	}

	public double scrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double scrollAmount) {
		this.scrollAmount = Mth.clamp(scrollAmount, 0.0D, this.maxScrollAmount());
	}

	public void refreshScrollAmount() {
		this.setScrollAmount(this.scrollAmount);
	}

	public int maxScrollAmount() {
		return Math.max(0, this.contentHeight() - this.getHeight());
	}

	public int scrollbarWidth() {
		return SCROLLBAR_WIDTH;
	}

	protected boolean scrollable() {
		return this.maxScrollAmount() > 0;
	}

	protected int scrollerHeight() {
		return Mth.clamp((int) ((float) (this.getHeight() * this.getHeight()) / this.contentHeight()), SCROLLER_MIN_HEIGHT, this.getHeight() - 8);
	}

	protected int scrollBarX() {
		return this.getX() + this.getWidth() - this.scrollbarWidth();
	}

	public int scrollBarY() {
		if (this.maxScrollAmount() == 0) {
			return this.getY();
		}
		return Math.max(this.getY(), (int) this.scrollAmount * (this.getHeight() - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
	}

	protected void extractScrollbar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		if (!this.scrollable()) {
			return;
		}

		int x = this.scrollBarX();
		int width = this.scrollbarWidth();
		int scrollerY = this.scrollBarY();
		int scrollerHeight = this.scrollerHeight();

		//? if >=1.21.1 {
		DrawUtils.drawGuiTexture(context, TransparencySprites.SCROLLER_BACKGROUND_SPRITE, x, this.getY(), width, this.getHeight());
		DrawUtils.drawGuiTexture(context, TransparencySprites.SCROLLER_SPRITE, x, scrollerY, width, scrollerHeight);
		//?} else {
		/^DrawUtils.drawGuiTexture(context, TransparencySprites.SCROLLER_BACKGROUND_SPRITE, x, this.getY(), width, this.getHeight(), SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_BORDER);
		DrawUtils.drawGuiTexture(context, TransparencySprites.SCROLLER_SPRITE, x, scrollerY, width, scrollerHeight, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_BORDER);
		^///?}
	}

	public boolean updateScrolling(double mouseX, double mouseY, int button) {
		this.scrolling = this.scrollable() && button == InputConstants.MOUSE_BUTTON_LEFT && this.isOverScrollbar(mouseX, mouseY);
		return this.scrolling;
	}

	protected boolean isOverScrollbar(double mouseX, double mouseY) {
		return mouseX >= this.scrollBarX()
				&& mouseX <= this.scrollBarX() + this.scrollbarWidth()
				&& mouseY >= this.getY()
				&& mouseY < this.getY() + this.getHeight();
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!this.scrolling) {
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}

		if (mouseY < this.getY()) {
			this.setScrollAmount(0.0D);
		} else if (mouseY > this.getY() + this.getHeight()) {
			this.setScrollAmount(this.maxScrollAmount());
		} else {
			double max = Math.max(1, this.maxScrollAmount());
			double scale = Math.max(1.0D, max / (this.getHeight() - this.scrollerHeight()));
			this.setScrollAmount(this.scrollAmount() + dragY * scale);
		}

		return true;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		this.scrolling = false;
	}

	//? if >=1.21.1 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!this.visible) {
			return false;
		}
		this.setScrollAmount(this.scrollAmount() - verticalAmount * this.scrollRate());
		return true;
	}
	//?} else {
	/^@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		if (!this.visible) {
			return false;
		}
		this.setScrollAmount(this.scrollAmount() - verticalAmount * this.scrollRate());
		return true;
	}
	^///?}
}

*///?}
