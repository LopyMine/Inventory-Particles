package net.lopymine.ip.editor.screen;

//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import java.util.*;
import java.util.function.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.editor.widget.EditorScrollArea;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.yacl.screen.CursorTooltipPositioner;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
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
	private final Map<Item, ItemStack> stacks = new IdentityHashMap<>();
	private final Set<Item> failedItems = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Predicate<Item> highlighted;
	private final Function<Item, List<Component>> tooltips;
	private final Consumer<Item> onSelect;

	@Getter
	@Nullable
	private Item selectedItem;

	@Nullable
	private Set<Item> selectedItems;
	@Nullable
	private Item anchorItem;
	@Nullable
	private Boolean dragSelection;

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
		if (this.selectedItems == null && this.selectedItem != null && !items.contains(this.selectedItem)) {
			this.select(null);
		}
	}

	public void setMultiSelection(Set<Item> selectedItems) {
		this.selectedItems = selectedItems;
	}

	private void select(@Nullable Item item) {
		if (this.selectedItems != null && item != null && !this.selectedItems.remove(item)) {
			this.selectedItems.add(item);
		}
		this.selectedItem = item;
		this.onSelect.accept(item);
	}

	private void setSelected(Item item, boolean selected) {
		if (this.selectedItems == null) {
			return;
		}
		boolean changed = selected ? this.selectedItems.add(item) : this.selectedItems.remove(item);
		if (changed) {
			this.selectedItem = item;
			this.onSelect.accept(item);
		}
	}

	private boolean selectRange(Item item) {
		if (this.selectedItems == null || this.anchorItem == null) {
			return false;
		}

		int from = this.items.indexOf(this.anchorItem);
		int to = this.items.indexOf(item);
		if (from == -1 || to == -1) {
			return false;
		}

		boolean selected = this.selectedItems.contains(this.anchorItem);
		for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
			this.setSelected(this.items.get(i), selected);
		}
		this.dragSelection = selected;
		return true;
	}

	private void dragSelect(double mouseX, double mouseY, double dragX, double dragY) {
		if (this.dragSelection == null) {
			return;
		}

		int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dragX), Math.abs(dragY)) / (CELL_SIZE / 4.0D)));
		for (int step = 1; step <= steps; step++) {
			double progress = step / (double) steps;
			Item item = this.getItemAt(mouseX - dragX * (1.0D - progress), mouseY - dragY * (1.0D - progress));
			if (item != null) {
				this.setSelected(item, this.dragSelection);
			}
		}
	}

	private boolean isSelected(Item item) {
		return this.selectedItems != null ? this.selectedItems.contains(item) : item == this.selectedItem;
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

				this.renderItem(context, item, x + 2, y + 2);

				if (this.highlighted.test(item)) {
					drawBorder(context, x + 1, y + 1, HIGHLIGHT_COLOR);
				}
				if (this.isSelected(item)) {
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
			CursorTooltipPositioner.render(context, this.getTooltip(hovered), mouseX, mouseY);
		}
	}

	private void renderItem(GuiGraphicsExtractor context, Item item, int x, int y) {
		if (this.failedItems.contains(item)) {
			DrawUtils.drawTexture(context, MissingTextureAtlasSprite.getLocation(), x, y, 0, 0, 16, 16, 16, 16);
			return;
		}

		//? if <1.21.11 {
		/*context.pose().pushPose();
		PoseStack.Pose pose = context.pose().last();
		*///?}
		try {
			context.item(this.stacks.computeIfAbsent(item, ItemStackUtils::create), x, y);
		} catch (Throwable throwable) {
			this.failedItems.add(item);
			InventoryParticlesClient.LOGGER.error("Failed to render item \"{}\" in the items grid", BuiltInRegistries.ITEM.getKey(item), throwable);
		}
		//? if <1.21.11 {
		/*while (context.pose().last() != pose) {
			context.pose().popPose();
		}
		context.pose().popPose();
		*///?}
	}

	private List<Component> getTooltip(Item item) {
		List<Component> tooltip;
		try {
			tooltip = new ArrayList<>(this.tooltips.apply(item));
		} catch (Throwable throwable) {
			tooltip = new ArrayList<>();
			tooltip.add(Component.literal(BuiltInRegistries.ITEM.getKey(item).toString()));
		}
		if (this.failedItems.contains(item)) {
			tooltip.add(InventoryParticles.text("editor.items.render_failed").withStyle(ChatFormatting.RED));
		}
		return tooltip;
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
		this.dragSelection = null;
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

		if (this.selectedItems == null) {
			this.select(item);
			return true;
		}

		if (InputUtils.hasShiftDown() && this.selectRange(item)) {
			return true;
		}

		this.select(item);
		this.anchorItem = item;
		this.dragSelection = this.selectedItems.contains(item);
		return true;
	}

	//? if >=1.21.11 {
	@Override
	protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		this.dragSelect(event.x(), event.y(), dragX, dragY);
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		super.onRelease(event);
		this.dragSelection = null;
	}
	//?} else {
	/*@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		this.dragSelect(mouseX, mouseY, dragX, dragY);
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		super.onRelease(mouseX, mouseY);
		this.dragSelection = null;
	}
	*///?}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}
}
