package net.lopymine.ip.editor.screen;

import java.util.*;
import java.util.function.IntConsumer;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;

public class DropdownWidget extends AbstractWidget {

	public static final int ROW_HEIGHT = 12;

	private static final String MORE = "...";
	private static final int BACKGROUND_COLOR = 0xF0100010;
	private static final int BORDER_COLOR = 0xFF5A5A5A;
	private static final int HOVER_COLOR = 0x40FFFFFF;
	private static final int SELECTED_COLOR = 0xFFFFDD00;
	private static final int MORE_COLOR = -6250336;

	private final List<Component> options = new ArrayList<>();
	private final IntConsumer onSelect;

	@Setter
	private int selected;
	private int scroll;

	public DropdownWidget(int x, int y, int width, int height, IntConsumer onSelect) {
		super(x, y, width, height, Component.empty());
		this.onSelect = onSelect;
		this.visible = false;
	}

	public void setOptions(List<Component> options, int selected, int maxHeight) {
		this.options.clear();
		this.options.addAll(options);
		this.selected = selected;
		this.scroll = 0;

		int rows = Math.max(1, Math.min(options.size(), (maxHeight - 2) / ROW_HEIGHT));
		this.height = rows * ROW_HEIGHT + 2;
	}

	public void toggle() {
		this.visible = !this.visible;
		this.scroll = 0;
	}

	private int getVisibleRows() {
		return Math.max(1, (this.getHeight() - 2) / ROW_HEIGHT);
	}

	private boolean hasMore() {
		return this.scroll + this.getVisibleRows() < this.options.size();
	}

	private int getShownRows() {
		return this.hasMore() ? this.getVisibleRows() - 1 : this.getVisibleRows();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		int x = this.getX();
		int y = this.getY();
		int right = x + this.getWidth();
		int bottom = y + this.getHeight();

		context.fill(x, y, right, bottom, BACKGROUND_COLOR);
		context.fill(x, y, right, y + 1, BORDER_COLOR);
		context.fill(x, bottom - 1, right, bottom, BORDER_COLOR);
		context.fill(x, y, x + 1, bottom, BORDER_COLOR);
		context.fill(right - 1, y, right, bottom, BORDER_COLOR);

		Font font = Minecraft.getInstance().font;
		int rows = this.getShownRows();

		for (int row = 0; row < rows; row++) {
			int index = this.scroll + row;
			if (index >= this.options.size()) {
				break;
			}

			int rowY = y + 1 + row * ROW_HEIGHT;
			if (mouseX >= x && mouseX < right && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				context.fill(x + 1, rowY, right - 1, rowY + ROW_HEIGHT, HOVER_COLOR);
			}

			context.text(font, this.options.get(index), x + 4, rowY + 2, index == this.selected ? SELECTED_COLOR : -1);
		}

		if (this.hasMore()) {
			context.text(font, Component.literal(MORE), x + 4, y + 1 + rows * ROW_HEIGHT + 2, MORE_COLOR);
		}
	}

	//? if >=1.21.11 {
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		double mouseX = event.x();
		double mouseY = event.y();
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
	*///?}
		if (!this.visible || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		int row = (int) ((mouseY - this.getY() - 1) / ROW_HEIGHT);
		if (row < 0 || row >= this.getShownRows()) {
			return true;
		}

		int index = this.scroll + row;
		if (index >= this.options.size()) {
			return true;
		}

		this.selected = index;
		this.visible = false;
		this.onSelect.accept(index);
		return true;
	}

	//? if >=1.21.1 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
	*///?}
		if (!this.visible || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		int max = Math.max(0, this.options.size() - this.getShownRows());
		this.scroll = Math.max(0, Math.min(max, this.scroll + (verticalAmount > 0.0D ? -1 : 1)));
		return true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) { }
}
