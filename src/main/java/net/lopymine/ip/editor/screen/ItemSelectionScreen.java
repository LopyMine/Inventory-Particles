package net.lopymine.ip.editor.screen;

import java.util.*;
import java.util.function.Consumer;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.editor.EditorItemsIndex;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.ip.utils.*;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemSelectionScreen extends EditorScreen {

	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int OFFSET = 6;
	private static final int BUTTON_OFFSET = 4;

	private static final int SOURCE_ALL = 0;
	private static final int SOURCE_INVENTORY = 1;
	private static final int SOURCE_NAMESPACE = 2;

	@Nullable
	private final Screen parent;
	private final EditorItemsIndex index;
	private final boolean onlyWithParticles;
	private final boolean highlightExisting;
	private final Consumer<Item> onPick;

	private EditBox searchWidget;
	@Nullable
	private Button filterButton;

	private Button sourceButton;
	private DropdownWidget sourceWidget;
	@Nullable
	private Set<Item> inventoryItems;

	private int source = SOURCE_ALL;
	private boolean showWithParticles = true;
	private ItemGridWidget gridWidget;
	private Button confirmButton;

	public ItemSelectionScreen(@Nullable Screen parent, Component title, EditorItemsIndex index, boolean onlyWithParticles, boolean highlightExisting, Consumer<Item> onPick) {
		super(title);
		this.parent = parent;
		this.index = index;
		this.onlyWithParticles = onlyWithParticles;
		this.highlightExisting = highlightExisting;
		this.onPick = onPick;
	}

	@Override
	protected void init() {
		int gridTop = 24 + BUTTON_HEIGHT + OFFSET;
		int gridBottom = this.height - BUTTON_HEIGHT - OFFSET * 2;

		int gridWidth = ItemGridWidget.widthFor(ItemGridWidget.fitColumns(this.width - OFFSET * 2));
		int gridHeight = ItemGridWidget.heightFor(ItemGridWidget.fitRows(gridBottom - gridTop));
		int gridX = (this.width - gridWidth) / 2;

		int buttonWidth = Math.min(BUTTON_WIDTH, gridWidth / 4);
		int searchWidth = gridWidth - (buttonWidth + BUTTON_OFFSET) * (this.highlightExisting ? 2 : 1);
		int sourceX = gridX + searchWidth + BUTTON_OFFSET;

		this.sourceButton = this.addRenderableWidget(Button.builder(this.getSourceText(), (button) -> this.sourceWidget.toggle())
				.bounds(sourceX, 24, buttonWidth, BUTTON_HEIGHT)
				.build());

		if (this.highlightExisting) {
			this.filterButton = this.addRenderableWidget(Button.builder(this.getFilterText(), (button) -> {
				this.showWithParticles = !this.showWithParticles;
				this.filterButton.setMessage(this.getFilterText());
				this.updateItems();
			}).bounds(sourceX + buttonWidth + BUTTON_OFFSET, 24, buttonWidth, BUTTON_HEIGHT).build());
		}

		this.searchWidget = new EditBox(this.font, gridX, 24, searchWidth, BUTTON_HEIGHT, InventoryParticles.text("editor.search"));
		this.searchWidget.setHint(InventoryParticles.text("editor.search").withStyle(ChatFormatting.WHITE));
		this.searchWidget.setResponder((value) -> this.updateItems());
		this.addRenderableWidget(this.searchWidget);

		this.gridWidget = new ItemGridWidget(
				gridX,
				gridTop + (gridBottom - gridTop - gridHeight) / 2,
				gridWidth,
				gridHeight,
				(item) -> this.highlightExisting && this.index.hasParticles(item),
				this::getItemTooltip,
				(item) -> this.confirmButton.active = item != null
		);
		this.addRenderableWidget(this.gridWidget);

		int y = gridBottom + OFFSET;
		this.confirmButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.continue"), (button) -> this.confirm())
				.bounds(this.width / 2 - BUTTON_WIDTH - 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
		this.confirmButton.active = false;

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose())
				.bounds(this.width / 2 + 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());

		int dropdownY = 24 + BUTTON_HEIGHT;
		this.sourceWidget = new DropdownWidget(sourceX, dropdownY, buttonWidth, 0, this::selectSource);
		this.sourceWidget.setOptions(this.getSourceLabels(), this.source, this.height - dropdownY - OFFSET);
		this.gridWidget.setOverlay(this.sourceWidget);

		this.updateItems();
	}

	private List<Component> getSourceLabels() {
		List<Component> labels = new ArrayList<>();
		labels.add(InventoryParticles.text("editor.items.source.all"));
		labels.add(InventoryParticles.text("editor.items.source.inventory"));
		for (String namespace : this.index.getNamespaces()) {
			labels.add(Component.literal(namespace));
		}

		if (MossyLoader.isDevelopmentEnvironment()) {
			int debugSources = 50;
			for (int i = 0; i < debugSources; i++) {
				labels.add(Component.literal(String.valueOf(i)));
			}
		}

		return labels;
	}

	private void selectSource(int source) {
		if (source >= SOURCE_NAMESPACE + this.index.getNamespaces().size()) {
			this.sourceWidget.setSelected(this.source);
			return;
		}

		this.source = source;
		this.sourceButton.setMessage(this.getSourceText());
		this.updateItems();
	}

	private Component getSourceText() {
		List<Component> labels = this.getSourceLabels();
		return InventoryParticles.text("editor.items.source", labels.get(Math.min(this.source, labels.size() - 1)));
	}

	private boolean matchesSource(Item item) {
		if (this.source == SOURCE_ALL) {
			return true;
		}
		if (this.source == SOURCE_INVENTORY) {
			return this.getInventoryItems().contains(item);
		}

		List<String> namespaces = this.index.getNamespaces();
		int index = this.source - SOURCE_NAMESPACE;
		return index < namespaces.size() && this.index.getId(item).getNamespace().equals(namespaces.get(index));
	}

	private Set<Item> getInventoryItems() {
		if (this.inventoryItems == null) {
			this.inventoryItems = Collections.newSetFromMap(new IdentityHashMap<>());

			LocalPlayer player = Minecraft.getInstance().player;
			Inventory inventory = player == null ? null : player.getInventory();
			for (int slot = 0; inventory != null && slot < inventory.getContainerSize(); slot++) {
				ItemStack stack = inventory.getItem(slot);
				if (!stack.isEmpty()) {
					this.inventoryItems.add(stack.getItem());
				}
			}
		}
		return this.inventoryItems;
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
		if (this.sourceWidget.visible) {
			//? if >=1.21.11 {
			if (this.sourceWidget.mouseClicked(event, doubled)) {
			//?} else {
			/*if (this.sourceWidget.mouseClicked(mouseX, mouseY, button)) {
			*///?}
				return true;
			}
			if (!this.sourceButton.isMouseOver(mouseX, mouseY)) {
				this.sourceWidget.visible = false;
				return true;
			}
		}
		//? if >=1.21.11 {
		return super.mouseClicked(event, doubled);
		//?} else {
		/*return super.mouseClicked(mouseX, mouseY, button);
		*///?}
	}

	//? if >=1.21.1 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.sourceWidget.visible && this.sourceWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		if (this.sourceWidget.visible && this.sourceWidget.mouseScrolled(mouseX, mouseY, verticalAmount)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, verticalAmount);
	}
	*///?}

	private List<Component> getItemTooltip(Item item) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(item.getName(ItemStackUtils.create(item)));
		tooltip.add(Component.literal(this.index.getId(item).toString()).withStyle(ChatFormatting.GRAY));

		if (this.highlightExisting && this.index.hasParticles(item)) {
			tooltip.add(InventoryParticles.text("editor.items.has_particles").withStyle(ChatFormatting.YELLOW));
		}

		return tooltip;
	}

	private Component getFilterText() {
		return InventoryParticles.text(this.showWithParticles ? "editor.items.filter.on" : "editor.items.filter.off");
	}

	private void updateItems() {
		List<Item> items = this.index.search(this.searchWidget.getValue(), this.onlyWithParticles).stream()
				.filter(this::matchesSource)
				.filter((item) -> this.showWithParticles || !this.index.hasParticles(item))
				.toList();
		this.gridWidget.setItems(items);
	}

	private void confirm() {
		Item item = this.gridWidget.getSelectedItem();
		if (item == null) {
			return;
		}
		this.onPick.accept(item);
	}

	@Override
	protected void renderPanels(GuiGraphicsExtractor context) {
		BackgroundRenderer.drawTransparencyBackground(context, this.gridWidget.getX(), this.gridWidget.getY(), this.gridWidget.getWidth(), this.gridWidget.getHeight(), true);
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		boolean blocked = this.sourceWidget.visible && this.sourceWidget.isMouseOver(mouseX, mouseY);

		this.renderScreen(context, blocked ? -1 : mouseX, blocked ? -1 : mouseY, deltaTicks);
		context.centeredText(this.font, this.title, this.width / 2, 8, -1);

		WidgetUtils.pushAbove(context);
		WidgetUtils.render(this.sourceWidget, context, mouseX, mouseY, deltaTicks);
		WidgetUtils.popAbove(context);
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}
}
