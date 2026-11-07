package net.lopymine.ip.editor.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.editor.*;
import net.lopymine.ip.editor.field.*;
import net.lopymine.ip.editor.preview.EditorParticlesRenderer;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.ip.utils.ItemStackUtils;
import net.lopymine.ip.yacl.screen.CursorTooltipPositioner;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ConfigEditorScreen extends EditorScreen {

	private static final int BUTTON_HEIGHT = 20;
	private static final int OFFSET = 6;
	private static final double MIN_TICK_RATE = 0.5D;
	private static final double MAX_TICK_RATE = 200.0D;

	@Nullable
	private final Screen parent;
	private final EditedConfig edited;
	private final EditorParticlesRenderer renderer = new EditorParticlesRenderer();

	@Nullable
	private final String focusItemId;

	private final Map<AbstractWidget, Supplier<List<Component>>> tooltips = new LinkedHashMap<>();

	private FieldsPanelWidget panelWidget;
	private Button pathButton;
	@Nullable
	private Button modeButton;
	private Button copyHoldersButton;
	private Button tooltipsButton;
	private EditBox tickRateWidget;

	private boolean copyWithHolders;
	private int activeHolderIndex;
	private int previewX;
	private int previewY;
	private int previewWidth;
	private int previewHeight;

	@Nullable
	private ParticleHolder previewHolder;

	private double itemX = -1;
	private double itemY = -1;
	private boolean holdingItem;
	private double ticksPerSecond = 20.0D;

	public ConfigEditorScreen(@Nullable Screen parent, EditedConfig edited, @Nullable String focusItemId) {
		super(InventoryParticles.text("editor.config.title"));
		this.parent = parent;
		this.edited = edited;
		this.focusItemId = focusItemId;
	}

	@Override
	protected void init() {
		this.tooltips.clear();

		int panelWidth = Math.max(200, (int) (this.width * 0.55F) - OFFSET * 2);
		int contentTop = 22 + BUTTON_HEIGHT + 2;
		int contentBottom = this.height - (BUTTON_HEIGHT + OFFSET) * 2;

		this.pathButton = this.addRenderableWidget(Button.builder(Component.literal(this.edited.getShortPath()), (button) -> this.rename())
				.bounds(OFFSET, 22, panelWidth / 2 - 2, BUTTON_HEIGHT)
				.build());
		this.withTooltip(this.pathButton, () -> EditorTooltips.of(
				InventoryParticles.text("editor.config.name.tooltip"),
				Component.literal(this.edited.getFullPath())
		));

		if (this.edited.hasParent()) {
			this.modeButton = this.addRenderableWidget(Button.builder(this.edited.getMode().getTitle(), (button) -> this.cycleMode())
					.bounds(OFFSET + panelWidth / 2 + 2, 22, panelWidth / 2 - 2, BUTTON_HEIGHT)
					.build());
			this.withTooltip(this.modeButton, () -> EditorTooltips.of(
					this.edited.getMode().getDescription(),
					InventoryParticles.text("editor.binding_mode.hint"),
					InventoryParticles.text("editor.binding_mode.link.hint"),
					InventoryParticles.text("editor.binding_mode.copy.hint"),
					InventoryParticles.text("editor.binding_mode.override.hint")
			));
		}

		this.panelWidget = new FieldsPanelWidget(OFFSET, contentTop, panelWidth, contentBottom - contentTop);
		this.addRenderableWidget(this.panelWidget);

		this.previewX = panelWidth + OFFSET * 2;
		this.previewY = contentTop;
		this.previewWidth = this.width - panelWidth - OFFSET * 3;
		this.previewHeight = contentBottom - contentTop;

		int resetWidth = Math.min(this.previewWidth, 140);
		Button resetPositionButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.config.preview.reset_position"), (button) -> this.resetItemPosition())
				.bounds(this.previewX + this.previewWidth - resetWidth, 22, resetWidth, BUTTON_HEIGHT)
				.build());
		this.withTooltip(resetPositionButton, () -> EditorTooltips.ofKeys(
				"editor.config.preview.reset_position.tooltip",
				"editor.config.preview.left_click",
				"editor.config.preview.right_click",
				"editor.config.preview.middle_click"
		));

		int buttonWidth = panelWidth / 2 - 2;
		int firstRow = contentBottom + OFFSET / 2;

		Button copyFromButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.config.copy_from"), (button) -> this.copyFromAnother())
				.bounds(OFFSET, firstRow, buttonWidth, BUTTON_HEIGHT)
				.build());
		this.withTooltip(copyFromButton, () -> EditorTooltips.ofKeys("editor.config.copy_from.tooltip"));

		this.copyHoldersButton = this.addRenderableWidget(Button.builder(this.getCopyHoldersText(), (button) -> {
			this.copyWithHolders = !this.copyWithHolders;
			this.copyHoldersButton.setMessage(this.getCopyHoldersText());
		}).bounds(OFFSET + buttonWidth + 4, firstRow, buttonWidth, BUTTON_HEIGHT).build());
		this.withTooltip(this.copyHoldersButton, () -> EditorTooltips.ofKeys(
				"editor.config.copy_with_holders.tooltip",
				"editor.config.copy_with_holders.off.hint",
				"editor.config.copy_with_holders.on.hint"
		));

		int tickRateWidth = 46;
		this.tickRateWidget = new EditBox(this.font, this.previewX + this.previewWidth - tickRateWidth, firstRow, tickRateWidth, BUTTON_HEIGHT, InventoryParticles.text("editor.config.preview.tick_rate"));
		this.tickRateWidget.setMaxLength(5);
		this.tickRateWidget.setValue(String.valueOf((int) this.ticksPerSecond));
		this.tickRateWidget.setResponder(this::onTickRateChanged);
		this.addRenderableWidget(this.tickRateWidget);

		int secondRow = firstRow + BUTTON_HEIGHT + 2;

		Button copyToNewButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.config.copy_to_new"), (button) -> this.copyToNew())
				.bounds(OFFSET, secondRow, buttonWidth, BUTTON_HEIGHT)
				.build());
		this.withTooltip(copyToNewButton, () -> EditorTooltips.ofKeys("editor.config.copy_to_new.tooltip"));

		this.tooltipsButton = this.addRenderableWidget(Button.builder(EditorTooltips.getToggleText(), (button) -> this.toggleTooltips())
				.bounds(OFFSET + buttonWidth + 4, secondRow, buttonWidth, BUTTON_HEIGHT)
				.build());

		this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.config.done"), (button) -> this.done())
				.bounds(this.previewX, secondRow, this.previewWidth / 2 - 2, BUTTON_HEIGHT)
				.build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose())
				.bounds(this.previewX + this.previewWidth / 2 + 2, secondRow, this.previewWidth / 2 - 2, BUTTON_HEIGHT)
				.build());

		if (!this.isItemInPreview()) {
			this.resetItemPosition();
		}

		this.rebuildPanel();
		this.focusHolder();
	}

	private void withTooltip(AbstractWidget widget, Supplier<List<Component>> tooltip) {
		this.tooltips.put(widget, tooltip);
	}

	private void toggleTooltips() {
		EditorTooltips.toggle();
		this.tooltipsButton.setMessage(EditorTooltips.getToggleText());
	}

	private void onTickRateChanged(String value) {
		try {
			double ticksPerSecond = Double.parseDouble(value.trim());
			if (ticksPerSecond < MIN_TICK_RATE || ticksPerSecond > MAX_TICK_RATE) {
				throw new NumberFormatException(value);
			}

			this.ticksPerSecond = ticksPerSecond;
			this.renderer.setTicksPerSecond(ticksPerSecond);
			this.tickRateWidget.setTextColor(FieldRowWidget.TEXT_COLOR);
		} catch (NumberFormatException e) {
			this.tickRateWidget.setTextColor(FieldRowWidget.INVALID_TEXT_COLOR);
		}
	}

	private void resetItemPosition() {
		this.itemX = this.previewX + this.previewWidth / 2.0D;
		this.itemY = this.previewY + this.previewHeight / 2.0D;
		this.holdingItem = false;
	}

	private boolean isItemInPreview() {
		return this.isPreviewHovered((int) this.itemX, (int) this.itemY);
	}

	private boolean isItemHovered(int mouseX, int mouseY) {
		return Math.abs(mouseX - this.itemX) <= 8 && Math.abs(mouseY - this.itemY) <= 8;
	}

	private Component getCopyHoldersText() {
		return InventoryParticles.text(this.copyWithHolders ? "editor.config.copy_with_holders.on" : "editor.config.copy_with_holders.off");
	}

	private void rebuildPanel() {
		ConfigFieldsBuilder builder = new ConfigFieldsBuilder(this.panelWidget, this::onConfigChanged, this::rebuildPanel, this.edited, this.focusItemId);
		this.panelWidget.setRows(builder.build(this.edited.getConfig()));
		this.updateActiveHolder();
		this.updatePreviewSource();
	}

	private void updateActiveHolder() {
		if (this.focusItemId == null) {
			return;
		}

		List<ParticleHolder> holders = this.getHolders();
		for (int i = 0; i < holders.size(); i++) {
			if (matchesItem(holders.get(i), this.focusItemId)) {
				this.activeHolderIndex = i;
				return;
			}
		}
	}

	private static boolean matchesItem(ParticleHolder holder, String itemId) {
		return holder.getItemOrTag().left()
				.map((cachedItem) -> cachedItem.getId().toString().equals(itemId))
				.orElse(false);
	}

	private void onConfigChanged() {
		this.edited.markDirty();
		this.updatePreviewSource();
	}

	private void updatePreviewSource() {
		this.previewHolder = this.getActiveHolder();
		this.renderer.setSource(this.edited.getConfig(), this.previewHolder == null ? List.of() : List.of(this.previewHolder));
	}

	private void refreshPreviewHolder() {
		if (this.getActiveHolder() != this.previewHolder) {
			this.updatePreviewSource();
		}
	}

	@Nullable
	private ParticleHolder getActiveHolder() {
		List<ParticleHolder> holders = this.getHolders();
		if (holders.isEmpty()) {
			return null;
		}

		int index = this.getFocusedHolderIndex();
		return holders.get(Math.min(Math.max(index, 0), holders.size() - 1));
	}

	private List<ParticleHolder> getHolders() {
		List<Object> holders = new ArrayList<>(this.edited.getConfig().getHolders());
		FieldPath.sort(holders);
		return holders.stream().map(ParticleHolder.class::cast).toList();
	}

	private int getFocusedHolderIndex() {
		if (this.focusItemId != null) {
			return this.activeHolderIndex;
		}

		GuiEventListener focused = this.panelWidget.getFocused();
		if (!(focused instanceof FieldRowWidget row)) {
			return this.activeHolderIndex;
		}

		GroupRowWidget group = row instanceof GroupRowWidget groupRow ? groupRow : row.getGroup();
		while (group != null) {
			Integer index = parseHolderIndex(group.getKey());
			if (index != null) {
				this.activeHolderIndex = index;
				return index;
			}
			group = group.getGroup();
		}

		return this.activeHolderIndex;
	}

	@Nullable
	private static Integer parseHolderIndex(String key) {
		if (!key.startsWith("holders[") || !key.endsWith("]")) {
			return null;
		}
		try {
			return Integer.parseInt(key.substring("holders[".length(), key.length() - 1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void focusHolder() {
		if (this.focusItemId == null) {
			return;
		}

		List<ParticleHolder> holders = this.getHolders();
		for (int i = 0; i < holders.size(); i++) {
			if (matchesItem(holders.get(i), this.focusItemId)) {
				this.activeHolderIndex = i;
				this.panelWidget.scrollToRow("holders[%s]".formatted(i));
				this.updatePreviewSource();
				return;
			}
		}
	}

	private void rename() {
		ScreenUtils.set(new NameInputScreen(
				this,
				InventoryParticles.text("editor.config.rename.title"),
				InventoryParticles.text("editor.config.name"),
				InventoryParticles.text("editor.config.rename.confirm"),
				this.edited.getPath(),
				this::isValidPath,
				(path) -> {
					this.edited.setPath(path);
					ScreenUtils.set(this);
				},
				(path) -> InventoryParticles.text("editor.config.name.subfolder_hint")
		));
	}

	private boolean isValidPath(String path) {
		if (path.isBlank() || path.startsWith("/") || path.endsWith("/") || path.contains("//")) {
			return false;
		}
		for (char c : path.toCharArray()) {
			if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '/' || c == '.') {
				continue;
			}
			return false;
		}
		return true;
	}

	private void copyFromAnother() {
		ScreenUtils.set(new ConfigSelectionScreen(
				this,
				InventoryParticles.text("editor.config.copy_from.title"),
				this.edited.getSession(),
				null,
				(entry) -> {
					EditorConfigsIndex.LoadedConfig loaded = EditorConfigsIndex.load(entry);
					if (loaded != null) {
						this.edited.copyFrom(entry, loaded.config(), this.copyWithHolders);
					}
					ScreenUtils.set(this);
				}
		));
	}

	private void copyToNew() {
		String copySuffix = "_copy_\\d+$";
		Set<String> paths = new HashSet<>();
		for (EditorConfigEntry entry : this.edited.getSession().getPackEntries()) {
			paths.add(entry.path());
		}
		paths.add(this.edited.getPath());

		String base = this.edited.getPath().replaceFirst(copySuffix, "");
		int number = 1;
		while (paths.contains("%s_copy_%s".formatted(base, number))) {
			number++;
		}

		this.edited.setPath("%s_copy_%s".formatted(base, number));
		this.pathButton.setMessage(Component.literal(this.edited.getShortPath()));
	}

	private void cycleMode() {
		if (this.modeButton == null) {
			return;
		}

		this.edited.setMode(this.edited.getMode().next());
		this.modeButton.setMessage(this.edited.getMode().getTitle());
		this.pathButton.setMessage(Component.literal(this.edited.getShortPath()));
		this.rebuildPanel();
	}

	private void done() {
		try {
			this.edited.apply();
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to save particle config \"{}\":", this.edited.getFullPath(), e);
		}
		ScreenUtils.set(this.parent);
	}

	private ItemStack getPreviewStack() {
		CachedItem item = this.getPreviewItem();
		return item == null || EditorItems.isUnknown(item) ? ItemStack.EMPTY : ItemStackUtils.create(item.getItem());
	}

	@Nullable
	private CachedItem getPreviewItem() {
		ParticleHolder holder = this.getActiveHolder();
		return holder == null ? null : holder.getItemOrTag().left().orElse(null);
	}

	private void renderUnknownItem(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		CachedItem item = this.getPreviewItem();
		if (item == null) {
			return;
		}

		int x = (int) this.itemX - 8;
		int y = (int) this.itemY - 8;
		EditorItems.renderUnknown(context, x, y);

		if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
			CursorTooltipPositioner.render(context, EditorItems.getUnknownTooltip(item.getId()), mouseX, mouseY);
		}
	}

	private boolean isPreviewHovered(int mouseX, int mouseY) {
		return mouseX >= this.previewX && mouseX <= this.previewX + this.previewWidth && mouseY >= this.previewY && mouseY <= this.previewY + this.previewHeight;
	}

	//? if >=1.21.11 {
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (this.handlePreviewClick((int) event.x(), (int) event.y(), event.button())) {
			return true;
		}
		return super.mouseClicked(event, doubled);
	}
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.handlePreviewClick((int) mouseX, (int) mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	*///?}

	private boolean handlePreviewClick(int mouseX, int mouseY, int button) {
		if (!this.isPreviewHovered(mouseX, mouseY) || !this.holdingItem && !this.isItemHovered(mouseX, mouseY)) {
			return false;
		}

		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			if (this.holdingItem) {
				this.itemX = mouseX;
				this.itemY = mouseY;
			}
			this.holdingItem = !this.holdingItem;
			return true;
		}

		if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
			this.renderer.spawn(this.itemX, this.itemY, this.getPreviewStack(), false);
			return true;
		}

		if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
			this.renderer.spawn(this.itemX, this.itemY, this.getPreviewStack(), true);
			return true;
		}

		return false;
	}

	@Override
	protected void renderPanels(GuiGraphicsExtractor context) {
		BackgroundRenderer.drawTransparencyBackground(context, this.previewX, this.previewY, this.previewWidth, this.previewHeight, true);
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.refreshPreviewHolder();

		if (this.holdingItem) {
			this.itemX = Mth.clamp(mouseX, this.previewX + 2, this.previewX + this.previewWidth - 2);
			this.itemY = Mth.clamp(mouseY, this.previewY + 2, this.previewY + this.previewHeight - 2);
		}

		this.renderScreen(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, 8, -1);
		context.text(this.font, InventoryParticles.text("editor.config.preview.tick_rate"), this.previewX, this.tickRateWidget.getY() + 6, -1);

		context.enableScissor(this.previewX + 2, this.previewY + 2, this.previewX + this.previewWidth - 2, this.previewY + this.previewHeight - 2);

		ItemStack stack = this.getPreviewStack();
		if (stack.isEmpty()) {
			this.renderUnknownItem(context, mouseX, mouseY);
		} else {
			context.item(stack, (int) this.itemX - 8, (int) this.itemY - 8);
		}

		this.renderer.advance(this.itemX, this.itemY, stack);
		this.renderer.render(context);
		context.disableScissor();

		this.extractTooltip(context, mouseX, mouseY);
	}

	private void extractTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		if (!EditorTooltips.isEnabled()) {
			return;
		}

		for (Map.Entry<AbstractWidget, Supplier<List<Component>>> entry : this.tooltips.entrySet()) {
			AbstractWidget widget = entry.getKey();
			if (widget.visible && widget.isMouseOver(mouseX, mouseY)) {
				CursorTooltipPositioner.render(context, entry.getValue().get(), mouseX, mouseY);
				return;
			}
		}
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}
}
