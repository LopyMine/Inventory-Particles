package net.lopymine.ip.gui.widget;

import java.util.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.gui.list.AbstractSubWidgetsWidget;
import net.lopymine.ip.utils.mixin.IDrawContextMixin;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;

@ExtensionMethod(DrawContextExtension.class)
public class ClippedPanelWidget extends AbstractSubWidgetsWidget<ClickableWidget> {

	private int clipX = 0;
	private int clipY = 0;

	public ClippedPanelWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.of(""));
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundRenderer.drawTransparencyBackground(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), false);

		context.enableScissor(this.getX() + 2, this.getY() + 2, this.getX() + this.getWidth() - 2, this.getY() + this.getHeight() - 2);
		context.push();
		context.translate(this.getX() + 2, this.getY() + 2, 0);
		for (ClickableWidget widget : this.widgets) {
			context.push();
			context.translate(this.clipX, this.clipY, 0);
			((IDrawContextMixin) context).inventoryParticles$mark();
			int mx = this.isHovered() ? (int) this.getMouseX(mouseX) : -1;
			int my = this.isHovered() ? (int) this.getMouseY(mouseY) : -1;
			widget.render(context, mx, my, deltaTicks);
			((IDrawContextMixin) context).inventoryParticles$unmark();
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
			context.drawTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(Text.of("You can move in this widget. Hold [Ctrl] and drag with your mouse to move!"), 100), mouseX, mouseY);
		}
	}

	@Override
	protected boolean rootMouseClicked(double mouseX, double mouseY, int button, int modifiers) {
		return hasControlDown();
	}

	@Override
	protected boolean rootMouseReleased(double mouseX, double mouseY, int button, int modifiers) {
		return hasControlDown();
	}

	@Override
	protected boolean rootMouseDragged(double mouseX, double mouseY, int button, int modifiers, double deltaX, double deltaY) {
		if (hasControlDown()) {
			this.moveClip((int) Math.ceil(deltaX), (int) Math.ceil(deltaY));
			return true;
		}
		return false;
	}

	@Override
	protected boolean rootMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return hasControlDown();
	}

	//? if >=1.21.9 {
	public static boolean hasControlDown() {
		if (InventoryParticlesClient.IS_MAC) {
			return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 343) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 347);
		} else {
			return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow(), 345);
		}
	}
	//?} else {
	/*public static boolean hasControlDown() {
		if (InventoryParticlesClient.IS_MAC) {
			return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 343) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 347);
		} else {
			return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 341) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 345);
		}
	}
	*///?}

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
		System.out.println(clipX + " " + clipY  + " -- " + x2 + " " + y2);
	}

	public boolean overflowsInWidth() {
		return this.getMaxWidth() > this.getWidth();
	}

	public boolean overflowsInHeight() {
		return this.getMaxHeight() > this.getHeight();
	}

	public int getMaxWidth() {
		int i = 0;
		for (ClickableWidget widget : this.widgets) {
			int right = widget.getX() + widget.getWidth();
			if (right > i) {
				i = right;
			}
		}
		return i;
	}

	public int getMaxHeight() {
		int i = 0;
		for (ClickableWidget widget : this.widgets) {
			int bottom = widget.getY() + widget.getHeight();
			if (bottom > i) {
				i = bottom;
			}
		}
		return i;
	}
}
