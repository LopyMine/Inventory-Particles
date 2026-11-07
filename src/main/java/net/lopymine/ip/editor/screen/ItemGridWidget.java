package net.lopymine.ip.editor.screen;

import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.editor.widget.EditorScrollArea;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.yacl.screen.CursorTooltipPositioner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemGridWidget extends EditorScrollArea {

	public static final int CELL_SIZE = 20;
	public static final int PADDING = 4;
	public static final int SCROLLBAR_INSET = 2;

	private static final int HIGHLIGHT_COLOR = 0xFFFFDD00;
	private static final int SELECTION_COLOR = 0xFFFFFFFF;

	private final List<Item> items = new ArrayList<>();
	private final Predicate<Item> highlighted;
	private final Function<Item, List<Component>> tooltips;
	private final Consumer<Item> onSelect;

	@Getter
	@Nullable
	private Item selectedItem;

	@Setter
	@Nullable
	private AbstractWidget overlay;

	public ItemGridWidget(int x, int y, int width, int height, Predicate<Item> highlighted, Function<Item, List<Component>> tooltips, Consumer<Item> onSelect) {
		super(x, y, width, height, Component.empty(), CELL_SIZE);
		this.highlighted = highlighted;
		this.tooltips = tooltips;
		this.onSelect = onSelect;
	}

	public static int fitColumns(int availableWidth) {
		return Math.max(1, (availableWidth - PADDING * 2 - EditorScrollArea.SCROLLBAR_WIDTH) / CELL_SIZE);
	}

	public static int fitRows(int availableHeight) {
		return Math.max(1, (availableHeight - PADDING * 2) / CELL_SIZE);
	}

	public static int widthFor(int columns) {
		return PADDING * 2 + columns * CELL_SIZE + EditorScrollArea.SCROLLBAR_WIDTH;
	}

	public static int heightFor(int rows) {
		return PADDING * 2 + rows * CELL_SIZE;
	}

	public void setItems(List<Item> items) {
		this.items.clear();
		this.items.addAll(items);
		this.setScrollAmount(0.0D);
		if (this.selectedItem != null && !items.contains(this.selectedItem)) {
			this.select(null);
		}
	}

	private void select(@Nullable Item item) {
		this.selectedItem = item;
		this.onSelect.accept(item);
	}

	private int getColumns() {
		return Math.max(1, (this.getWidth() - PADDING * 2 - this.scrollbarWidth()) / CELL_SIZE);
	}

	private int getRows() {
		return Math.max(1, (this.getHeight() - PADDING * 2) / CELL_SIZE);
	}

	private int getTotalRows() {
		return (int) Math.ceil(this.items.size() / (double) this.getColumns());
	}

	@Override
	protected int contentHeight() {
		return this.getTotalRows() * CELL_SIZE + PADDING * 2;
	}

	@Override
	protected int scrollBarX() {
		return this.getX() + this.getWidth() - PADDING + 2 - this.scrollbarWidth();
	}

	@Override
	protected void extractScrollbar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		this.setY(this.getY() + SCROLLBAR_INSET);
		this.height -= SCROLLBAR_INSET * 2;

		super.extractScrollbar(context, mouseX, mouseY);

		this.height += SCROLLBAR_INSET * 2;
		this.setY(this.getY() - SCROLLBAR_INSET);
	}

	@Nullable
	private Item getItem(int index) {
		return index >= 0 && index < this.items.size() ? this.items.get(index) : null;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		int scroll = (int) this.scrollAmount();
		int columns = this.getColumns();
		int top = this.getY() + PADDING;
		int left = this.getX() + PADDING;

		context.enableScissor(this.getX(), top, this.getX() + this.getWidth(), this.getY() + this.getHeight() - PADDING);

		int firstRow = scroll / CELL_SIZE;
		for (int row = firstRow; row <= firstRow + this.getRows(); row++) {
			for (int column = 0; column < columns; column++) {
				Item item = this.getItem(row * columns + column);
				if (item == null) {
					continue;
				}

				int x = left + column * CELL_SIZE;
				int y = top + row * CELL_SIZE - scroll;

				context.item(ItemStackUtils.create(item), x + 2, y + 2);

				if (this.highlighted.test(item)) {
					drawBorder(context, x + 1, y + 1, HIGHLIGHT_COLOR);
				}
				if (item == this.selectedItem) {
					drawBorder(context, x + 1, y + 1, SELECTION_COLOR);
				}
			}
		}

		context.disableScissor();

		this.extractScrollbar(context, mouseX, mouseY);

		if (this.items.isEmpty()) {
			context.centeredText(Minecraft.getInstance().font, InventoryParticles.text("editor.items.empty"), this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2, -6250336);
			return;
		}

		Item hovered = this.getItemAt(mouseX, mouseY);
		if (hovered != null) {
			CursorTooltipPositioner.render(context, this.tooltips.apply(hovered), mouseX, mouseY);
		}
	}

	@Nullable
	private Item getItemAt(double mouseX, double mouseY) {
		if (!this.isMouseOver(mouseX, mouseY) || mouseX >= this.scrollBarX() || this.isOverlayHovered(mouseX, mouseY)) {
			return null;
		}

		int column = (int) ((mouseX - this.getX() - PADDING) / CELL_SIZE);
		int row = (int) ((mouseY - this.getY() - PADDING + this.scrollAmount()) / CELL_SIZE);
		if (column < 0 || column >= this.getColumns() || row < 0) {
			return null;
		}

		return this.getItem(row * this.getColumns() + column);
	}

	private boolean isOverlayHovered(double mouseX, double mouseY) {
		return this.overlay != null && this.overlay.visible && this.overlay.isMouseOver(mouseX, mouseY);
	}

	private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int color) {
		int size = CELL_SIZE - 2;
		context.fill(x, y, x + size, y + 1, color);
		context.fill(x, y + size - 1, x + size, y + size, color);
		context.fill(x, y, x + 1, y + size, color);
		context.fill(x + size - 1, y, x + size, y + size, color);
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
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		//? if >=1.21.11 {
		if (this.updateScrolling(event)) {
		//?} else {
		/*if (this.updateScrolling(mouseX, mouseY, button)) {
		*///?}
			return true;
		}

		Item item = this.getItemAt(mouseX, mouseY);
		if (item == null) {
			return false;
		}

		this.select(item);
		return true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}
}
