package net.lopymine.ip.yacl.screen;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

@ExtensionMethod(DrawContextExtension.class)
public class ClippedPanelWidget extends AbstractSubWidgetsWidget<AbstractWidget> {

	private int clipX = 0;
	private int clipY = 0;

	public ClippedPanelWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.literal(""));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundRenderer.drawTransparencyBackground(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), false);

		context.enableScissor(this.getX() + 2, this.getY() + 2, this.getX() + this.getWidth() - 2, this.getY() + this.getHeight() - 2);
		context.push();
		context.translate(this.getX() + 2, this.getY() + 2, 0);
		for (AbstractWidget widget : this.widgets) {
			context.push();
			context.translate(this.clipX, this.clipY, 0);
			int mx = this.isHovered() ? (int) this.getMouseX(mouseX) : -1;
			int my = this.isHovered() ? (int) this.getMouseY(mouseY) : -1;
			widget.extractRenderState(context, mx, my, deltaTicks);
			context.pop();
		}
		context.pop();
		context.disableScissor();

		int a = this.getX() + this.getWidth() - 16 - 5;
		int c = this.getY() + 5;
		if (this.overflowsInHeight() || this.overflowsInWidth()) {
			DrawUtils.drawTexture(context, InventoryParticles.id("textures/gui/clip/clip.png"), a, c, 0, 0, 16, 16, 16, 16);
		}
		if (mouseX > a && mouseX < a + 16 && mouseY > c && mouseY < c + 16) {
			context.setTooltipForNextFrame(Minecraft.getInstance().font, Component.literal("You can move in this widget. Hold [Ctrl] and drag with your mouse to move!"), mouseX, mouseY);
		}
	}

	@Override
	protected boolean rootMouseClicked(double mouseX, double mouseY, int button, int modifiers) {
		return Minecraft.getInstance().hasControlDown();
	}

	@Override
	protected boolean rootMouseReleased(double mouseX, double mouseY, int button, int modifiers) {
		return Minecraft.getInstance().hasControlDown();
	}

	@Override
	protected boolean rootMouseDragged(double mouseX, double mouseY, int button, int modifiers, double deltaX, double deltaY) {
		if (Minecraft.getInstance().hasControlDown()) {
			this.moveClip((int) Math.ceil(deltaX), (int) Math.ceil(deltaY));
			return true;
		}
		return false;
	}

	@Override
	protected boolean rootMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return Minecraft.getInstance().hasControlDown();
	}

	@Override
	protected double getMouseY(double mouseY) {
		return mouseY - this.clipY - this.getY() - 2;
	}

	@Override
	protected double getMouseX(double mouseX) {
		return mouseX - this.clipX - this.getX() - 2;
	}

	private void moveClip(int deltaX, int deltaY) {
		int x = this.clipX + deltaX;
		int y = this.clipY + deltaY;
		int x2 = x - this.width + 5;
		int y2 = y - this.height + 5;
		if (x <= 0 && x2 > -this.getMaxWidth()) {
			this.clipX = x;
		}
		if (y <= 0 && y2 > -this.getMaxHeight()) {
			this.clipY = y;
		}
	}

	public boolean overflowsInWidth() {
		return this.getMaxWidth() > this.getWidth();
	}

	public boolean overflowsInHeight() {
		return this.getMaxHeight() > this.getHeight();
	}

	public int getMaxWidth() {
		int i = 0;
		for (AbstractWidget widget : this.widgets) {
			int right = widget.getX() + widget.getWidth();
			if (right > i) {
				i = right;
			}
		}
		return i;
	}

	public int getMaxHeight() {
		int i = 0;
		for (AbstractWidget widget : this.widgets) {
			int bottom = widget.getY() + widget.getHeight();
			if (bottom > i) {
				i = bottom;
			}
		}
		return i;
	}
}
