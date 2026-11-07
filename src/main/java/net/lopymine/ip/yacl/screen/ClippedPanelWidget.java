package net.lopymine.ip.yacl.screen;

import java.util.List;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.utils.*;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(DrawContextExtension.class)
public class ClippedPanelWidget extends AbstractSubWidgetsWidget<AbstractWidget> {

	private static final int HINT_SIZE = 16;

	@Getter
	private int clipX = 0;
	@Getter
	private int clipY = 0;

	private double dragResidualX;
	private double dragResidualY;

	public ClippedPanelWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Component.literal(""));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundRenderer.drawTransparencyBackground(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), false);

		context.enableScissor(this.getX() + 2, this.getY() + 2, this.getX() + this.getWidth() - 2, this.getY() + this.getHeight() - 2);
		for (AbstractWidget widget : this.widgets) {
			if (!widget.visible || this.isOutOfView(widget)) {
				continue;
			}
			WidgetUtils.render(widget, context, mouseX, mouseY, deltaTicks);
		}
		context.disableScissor();

		this.extractWidgetsTooltip(context, mouseX, mouseY);

		int a = this.getX() + this.getWidth() - HINT_SIZE - 5;
		int c = this.getY() + this.getHeight() - HINT_SIZE - 5;
		if (this.overflowsInHeight() || this.overflowsInWidth()) {
			WidgetUtils.pushAbove(context);
			DrawUtils.drawTexture(context, InventoryParticles.id("textures/gui/clip/clip.png"), a, c, 0, 0, HINT_SIZE, HINT_SIZE, HINT_SIZE, HINT_SIZE);
			WidgetUtils.popAbove(context);
		}
		if (mouseX > a && mouseX < a + HINT_SIZE && mouseY > c && mouseY < c + HINT_SIZE) {
			CursorTooltipPositioner.render(context, List.of(Component.literal("You can move in this widget."), Component.literal("Hold [Ctrl] and drag with your mouse to move!")), mouseX, mouseY);
		}
	}

	private void extractWidgetsTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		if (!this.isHovered()) {
			return;
		}

		for (AbstractWidget widget : this.widgets) {
			if (!widget.visible || this.isOutOfView(widget) || !(widget instanceof TooltipProvider provider)) {
				continue;
			}

			List<Component> tooltip = provider.getClippedTooltip(mouseX, mouseY);
			if (tooltip == null || tooltip.isEmpty()) {
				continue;
			}

			CursorTooltipPositioner.render(context, tooltip, mouseX, mouseY);
			return;
		}
	}

	@Override
	protected boolean rootMouseClicked(double mouseX, double mouseY, int button) {
		this.dragResidualX = 0.0D;
		this.dragResidualY = 0.0D;
		return InputUtils.hasControlDown();
	}

	@Override
	protected boolean rootMouseReleased(double mouseX, double mouseY, int button) {
		return InputUtils.hasControlDown();
	}

	@Override
	protected boolean rootMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!InputUtils.hasControlDown()) {
			return false;
		}

		this.dragResidualX += deltaX;
		this.dragResidualY += deltaY;

		int x = (int) this.dragResidualX;
		int y = (int) this.dragResidualY;
		this.dragResidualX -= x;
		this.dragResidualY -= y;

		if (x != 0 || y != 0) {
			this.moveClip(x, y);
		}
		return true;
	}

	@Override
	protected boolean rootMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!this.overflowsInHeight() && !this.overflowsInWidth()) {
			return InputUtils.hasControlDown();
		}
		int amount = (int) Math.ceil(Math.abs(verticalAmount) * 10) * (verticalAmount < 0 ? -1 : 1);
		if (InputUtils.hasShiftDown()) {
			this.moveClip(amount, 0);
		} else {
			this.moveClip(0, amount);
		}
		return true;
	}

	public boolean isOutOfView(AbstractWidget widget) {
		return widget.getY() + widget.getHeight() < this.getY()
				|| widget.getY() > this.getY() + this.getHeight()
				|| widget.getX() + widget.getWidth() < this.getX()
				|| widget.getX() > this.getX() + this.getWidth();
	}

	public void scrollTo(AbstractWidget widget) {
		int top = widget.getY() - this.getY() - 2;
		if (top >= 0 && top + widget.getHeight() <= this.getHeight() - 4) {
			return;
		}
		this.setClip(this.clipX, this.clipY - top + 4);
	}

	public void resetClip() {
		this.setClip(0, 0);
	}

	public void setClip(int x, int y) {
		this.clipX = clampClip(x, this.getWidth(), this.getMaxWidth());
		this.clipY = clampClip(y, this.getHeight(), this.getMaxHeight());
		this.onClipChanged();
	}

	protected void updateClip() {
		this.clipX = clampClip(this.clipX, this.getWidth(), this.getMaxWidth());
		this.clipY = clampClip(this.clipY, this.getHeight(), this.getMaxHeight());
	}

	private static int clampClip(int clip, int size, int maxSize) {
		if (maxSize <= size) {
			return 0;
		}
		return Math.min(0, Math.max(clip, size - 5 - maxSize));
	}

	protected void onClipChanged() {

	}

	public void moveClip(int deltaX, int deltaY) {
		this.setClip(this.clipX + deltaX, this.clipY + deltaY);
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

	public interface TooltipProvider {

		@Nullable
		List<Component> getClippedTooltip(int mouseX, int mouseY);

	}
}
