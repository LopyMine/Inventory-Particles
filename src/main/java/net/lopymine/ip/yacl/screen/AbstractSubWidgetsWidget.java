package net.lopymine.ip.yacl.screen;

import java.util.*;
import lombok.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class AbstractSubWidgetsWidget<T extends AbstractWidget> extends AbstractWidget implements ContainerEventHandler {

	protected final List<T> widgets = new ArrayList<>();

	private GuiEventListener focused;
	private boolean dragging;

	public AbstractSubWidgetsWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	public void addWidget(T widget) {
		this.widgets.add(widget);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return new ArrayList<>(this.widgets);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseClicked(event.x(), event.y(), event.button(), event.modifiers())) {
			return true;
		}
		return super.mouseClicked(new MouseButtonEvent(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()), doubled);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseReleased(event.x(), event.y(), event.button(), event.modifiers())) {
			return true;
		}
		return ContainerEventHandler.super.mouseReleased(new MouseButtonEvent(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()));
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
		if (!this.visible || !this.active || !this.isMouseOver(event.x(), event.y())) {
			return false;
		}
		if (this.rootMouseDragged(event.x(), event.y(), event.button(), event.modifiers(), offsetX, offsetY)) {
			return true;
		}
		return ContainerEventHandler.super.mouseDragged(new MouseButtonEvent(this.getMouseX(event.x()), this.getMouseY(event.y()), event.buttonInfo()), offsetX, offsetY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}
		return ContainerEventHandler.super.mouseScrolled(this.getMouseX(mouseX), this.getMouseY(mouseY), horizontalAmount, verticalAmount);
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
	public GuiEventListener getFocused() {
		return this.focused;
	}

	public void setFocused(@Nullable GuiEventListener focused) {
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
