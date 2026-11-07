package net.lopymine.ip.yacl.screen;

//? if <1.21.11 {
/*import com.mojang.blaze3d.platform.InputConstants;
*///?}
import java.util.*;
import lombok.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
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

	//? if >=1.21.11 {
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
	*///?}
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		//? if >=1.21.11 {
		boolean clicked = ContainerEventHandler.super.mouseClicked(new MouseButtonEvent(this.getMouseX(mouseX), this.getMouseY(mouseY), event.buttonInfo()), doubled);
		//?} else {
		/*boolean clicked = this.clickChild(this.getMouseX(mouseX), this.getMouseY(mouseY), button);
		*///?}
		if (!clicked) {
			this.setFocused((GuiEventListener) null);
		}
		return clicked;
	}

	//? if <1.21.11 {
	/*private boolean clickChild(double mouseX, double mouseY, int button) {
		GuiEventListener child = this.getChildAt(mouseX, mouseY).orElse(null);
		if (child == null) {
			return false;
		}

		if (child.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(child);
			if (button == InputConstants.MOUSE_BUTTON_LEFT) {
				this.setDragging(true);
			}
		}

		return true;
	}
	*///?}

	//? if >=1.21.11 {
	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
	//?} else {
	/*@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
	*///?}
		if (!this.visible || !this.active) {
			return false;
		}
		if (this.rootMouseReleased(mouseX, mouseY, button)) {
			return true;
		}
		//? if >=1.21.11 {
		return ContainerEventHandler.super.mouseReleased(new MouseButtonEvent(this.getMouseX(mouseX), this.getMouseY(mouseY), event.buttonInfo()));
		//?} else {
		/*return ContainerEventHandler.super.mouseReleased(this.getMouseX(mouseX), this.getMouseY(mouseY), button);
		*///?}
	}

	//? if >=1.21.11 {
	@Override
	public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
	//?} else {
	/*@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double offsetX, double offsetY) {
	*///?}
		if (!this.visible || !this.active) {
			return false;
		}
		if (this.rootMouseDragged(mouseX, mouseY, button, offsetX, offsetY)) {
			return true;
		}
		//? if >=1.21.11 {
		return ContainerEventHandler.super.mouseDragged(new MouseButtonEvent(this.getMouseX(mouseX), this.getMouseY(mouseY), event.buttonInfo()), offsetX, offsetY);
		//?} else {
		/*return ContainerEventHandler.super.mouseDragged(this.getMouseX(mouseX), this.getMouseY(mouseY), button, offsetX, offsetY);
		*///?}
	}

	//? if >=1.21.1 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		double horizontalAmount = 0.0D;
	*///?}
		if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		if (this.rootMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}
		//? if >=1.21.1 {
		return ContainerEventHandler.super.mouseScrolled(this.getMouseX(mouseX), this.getMouseY(mouseY), horizontalAmount, verticalAmount);
		//?} else {
		/*return ContainerEventHandler.super.mouseScrolled(this.getMouseX(mouseX), this.getMouseY(mouseY), verticalAmount);
		*///?}
	}

	protected boolean rootMouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	protected boolean rootMouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	protected boolean rootMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
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
		if (this.focused == focused) {
			return;
		}

		if (this.focused != null) {
			this.focused.setFocused(false);
		}

		if (focused != null) {
			focused.setFocused(true);
		}

		this.focused = focused;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setFocused((GuiEventListener) null);
		}
	}

	@Override
	public void playDownSound(SoundManager soundManager) {

	}

}
