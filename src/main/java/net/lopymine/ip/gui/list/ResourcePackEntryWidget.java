package net.lopymine.ip.gui.list;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.ElementListWidget.Entry;
import net.minecraft.text.Text;

import java.util.List;

@Getter
public class ResourcePackEntryWidget extends Entry<ResourcePackEntryWidget> {

	private final ButtonWidget widget;

	public ResourcePackEntryWidget(Text text, PressAction pressAction) {
		this.widget = ButtonWidget.builder(text, pressAction).build();
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return List.of(this.widget);
	}

	@Override
	public List<? extends Element> children() {
		return List.of(this.widget);
	}

	//? if >=1.21.9 {
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		this.widget.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		this.widget.setWidth(width);
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		this.widget.setHeight(height);
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		this.widget.setX(x);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		this.widget.setY(y);
	}
	//?} else {
	/*@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		this.widget.setWidth(entryWidth);
		this.widget.setX(x);

		//? if >=1.21 {
		this.widget.setHeight(entryHeight);
		this.widget.setY(y);
		//?} else {
		/^this.widget.setY(y + ((entryHeight - 20) / 2));
		 ^///?}
		this.widget.render(context, mouseX, mouseY, tickDelta);
	}
	*///?}

	//? if >=1.21.9 {
	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return this.widget.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		return this.widget.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(Click click) {
		return this.widget.mouseReleased(click);
	}
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.widget.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return this.widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return this.widget.mouseReleased(mouseX, mouseY, button);
	}
	*///?}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.widget.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.widget.isMouseOver(mouseX, mouseY);
	}
}
