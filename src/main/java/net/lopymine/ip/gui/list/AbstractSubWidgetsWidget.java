package net.lopymine.ip.gui.list;

import java.util.*;
import lombok.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractSubWidgetsWidget<T extends ClickableWidget> extends ClickableWidget implements ParentElement {

	protected final List<T> widgets = new ArrayList<>();

	private Element focused;
	private boolean dragging;

	public AbstractSubWidgetsWidget(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}

	public void addWidget(T widget) {
		this.widgets.add(widget);
	}

	@Override
	public List<? extends Element> children() {
		return new ArrayList<>(this.widgets);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	//? if >=1.21.9 {
	@Override
	public boolean mouseClicked(Click event, boolean doubled) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseClicked(event.x(), event.y(), event.button(), event.modifiers())) {
			return true;
		}
		return super.mouseClicked(new Click(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()), doubled);
	}

	@Override
	public boolean mouseReleased(Click event) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseClicked(event.x(), event.y(), event.button(), event.modifiers())) {
			return true;
		}
		return ParentElement.super.mouseReleased(new Click(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()));
	}

	@Override
	public boolean mouseDragged(Click event, double offsetX, double offsetY) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseClicked(event.x(), event.y(), event.button(), event.modifiers())) {
			return true;
		}
		return ParentElement.super.mouseDragged(new Click(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()), offsetX, offsetY);
	}
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		ParentElement.super.mouseClicked(this.getMouseX(mouseX), this.getMouseY(mouseY), button);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseReleased(mouseX, mouseY, button)) {
			return true;
		}
		return ParentElement.super.mouseReleased(this.getMouseX(mouseX), this.getMouseY(mouseY), button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		return ParentElement.super.mouseDragged(this.getMouseX(mouseX), this.getMouseY(mouseY), button, deltaX, deltaY);
	}
	*///?}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}
		return ParentElement.super.mouseScrolled(this.getMouseX(mouseX), this.getMouseY(mouseY), horizontalAmount, verticalAmount);
	}

	protected boolean rootMouseClicked(double mouseX, double mouseY, int button, int modifiers) {
		return false;
	}

	protected boolean rootMouseReleased(double mouseX, double mouseY, int button, int modifiers) {
		return false;
	}

	protected boolean rootMouseDragged(double mouseX, double mouseY, int button, int modifiers, double deltaX, double deltaY) {
		return false;
	}

	protected boolean rootMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return false;
	}

	protected double getMouseX(double mouseX) {
		return mouseX;
	}

	protected double getMouseY(double mouseY) {
		return mouseY;
	}

	@Override
	public boolean isDragging() {
		return this.dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Nullable
	public Element getFocused() {
		return this.focused;
	}

	public void setFocused(@Nullable Element focused) {
		if (this.focused != null) {
			this.focused.setFocused(false);
		}

		if (focused != null) {
			focused.setFocused(true);
		}

		this.focused = focused;
	}

	@Override
	public void playDownSound(SoundManager soundManager) {

	}

}
