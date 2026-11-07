package net.lopymine.ip.editor.screen;

import java.util.*;
import java.util.function.Consumer;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.particle.ParticleConfig;
import net.lopymine.ip.editor.*;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.ip.element.texture.ITexture;
import net.lopymine.ip.renderer.OptimizedDrawer;
import net.lopymine.ip.yacl.screen.CursorTooltipPositioner;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class ConfigSelectionScreen extends EditorScreen {

	private static final int BUTTON_HEIGHT = 20;
	private static final int OFFSET = 6;
	private static final int ENTRY_HEIGHT = 24;
	private static final int BORDER = 2;

	@Nullable
	private final Screen parent;
	private final PackEditSession session;
	private final Consumer<EditorConfigEntry> onPick;
	@Nullable
	private final String itemFilter;

	private boolean searchByItem;
	private boolean includeOtherPacks;

	private EditBox searchWidget;
	private ConfigsListWidget listWidget;
	private HoldersPreviewWidget previewWidget;
	private Button confirmButton;

	public ConfigSelectionScreen(@Nullable Screen parent, Component title, PackEditSession session, @Nullable String itemFilter, Consumer<EditorConfigEntry> onPick) {
		super(title);
		this.parent = parent;
		this.session = session;
		this.onPick = onPick;
		this.itemFilter = itemFilter;
		this.includeOtherPacks = itemFilter != null;
	}

	@Override
	protected void init() {
		int listWidth = Math.max(160, this.width / 2 - OFFSET * 2);
		int contentTop = 22;
		int contentBottom = this.height - BUTTON_HEIGHT - OFFSET * 2;

		this.searchWidget = new EditBox(this.font, OFFSET, contentTop, listWidth, BUTTON_HEIGHT, InventoryParticles.text("editor.search"));
		this.searchWidget.setHint(InventoryParticles.text("editor.search").withStyle(ChatFormatting.WHITE));
		this.searchWidget.setResponder((ignored) -> this.updateEntries());
		this.addRenderableWidget(this.searchWidget);

		int y = contentTop + BUTTON_HEIGHT + 2;
		this.addRenderableWidget(Button.builder(this.getSearchModeText(), (button) -> {
			this.searchByItem = !this.searchByItem;
			button.setMessage(this.getSearchModeText());
			this.updateEntries();
		}).bounds(OFFSET, y, listWidth / 2 - 2, BUTTON_HEIGHT).build());

		this.addRenderableWidget(Button.builder(this.getOtherPacksText(), (button) -> {
			this.includeOtherPacks = !this.includeOtherPacks;
			button.setMessage(this.getOtherPacksText());
			this.updateEntries();
		}).bounds(OFFSET + listWidth / 2 + 2, y, listWidth / 2 - 2, BUTTON_HEIGHT).build());

		int listTop = y + BUTTON_HEIGHT + 2;
		this.listWidget = new ConfigsListWidget(this.minecraft, listWidth, contentBottom - listTop, listTop);
		this.addRenderableWidget(this.listWidget);

		this.previewWidget = new HoldersPreviewWidget(listWidth + OFFSET * 2, contentTop, this.width - listWidth - OFFSET * 3, contentBottom - contentTop);
		this.addRenderableWidget(this.previewWidget);

		int buttonY = contentBottom + OFFSET;
		int buttonWidth = listWidth / 2 - 2;
		this.confirmButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.continue"), (ignored) -> this.confirm())
				.bounds(OFFSET, buttonY, buttonWidth, BUTTON_HEIGHT)
				.build());
		this.confirmButton.active = false;

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (ignored) -> this.onClose())
				.bounds(OFFSET + buttonWidth + 4, buttonY, buttonWidth, BUTTON_HEIGHT)
				.build());

		this.updateEntries();
	}

	private Component getSearchModeText() {
		return InventoryParticles.text(this.searchByItem ? "editor.selection.search_by_item" : "editor.selection.search_by_path");
	}

	private Component getOtherPacksText() {
		return InventoryParticles.text(this.includeOtherPacks ? "editor.selection.other_packs.on" : "editor.selection.other_packs.off");
	}

	private void updateEntries() {
		List<EditorConfigEntry> entries = new ArrayList<>(this.session.getPackEntries());
		if (this.includeOtherPacks) {
			entries.addAll(this.session.getOtherEntries());
		}

		String query = this.searchWidget.getValue().trim();
		List<EditorConfigEntry> filtered = new ArrayList<>();

		for (EditorConfigEntry entry : entries) {
			if (this.itemFilter != null && !entry.itemIds().contains(this.itemFilter)) {
				continue;
			}
			if (!query.isEmpty() && !(this.searchByItem ? entry.matchesItem(query) : entry.matchesPath(query))) {
				continue;
			}
			filtered.add(entry);
		}

		this.listWidget.setEntries(filtered);
		this.confirmButton.active = false;
		this.previewWidget.setConfig(null);
	}

	private void confirm() {
		ConfigEntryWidget selected = this.listWidget.getSelected();
		if (selected == null) {
			return;
		}
		this.onPick.accept(selected.entry);
	}

	@Override
	protected void renderPanels(GuiGraphicsExtractor context) {
		BackgroundRenderer.drawTransparencyBackground(
				context,
				this.listWidget.getX() - BORDER,
				this.listWidget.getY() - BORDER,
				this.listWidget.getWidth() + BORDER * 2,
				this.listWidget.getHeight() + BORDER * 2,
				true
		);
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.renderScreen(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, 8, -1);

		if (this.listWidget.children().isEmpty()) {
			Component hint = InventoryParticles.text(this.includeOtherPacks ? "editor.selection.nothing_found" : "editor.selection.empty_pack");
			context.centeredText(this.font, hint, this.listWidget.getX() + this.listWidget.getWidth() / 2, this.listWidget.getY() + 20, -6250336);
		}
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}

	private class ConfigsListWidget extends EditorList<ConfigEntryWidget> {

		public ConfigsListWidget(Minecraft minecraft, int width, int height, int y) {
			super(minecraft, OFFSET + BORDER, y + BORDER, width - BORDER * 2, height - BORDER * 2, ENTRY_HEIGHT);
			this.disableChrome();
		}

		@Override
		public int getRowWidth() {
			return this.width - 30;
		}

		//? if >=1.21.11 {
		@Override
		protected int scrollBarX() {
			int scrollbarOffset = 1;
			return super.scrollBarX() + scrollbarOffset;
		}
		//?} else {
		/*@Override
		protected int getScrollbarPosition() {
			int scrollbarOffset = -1;
			return super.getScrollbarPosition() + scrollbarOffset;
		}
		*///?}

		//? if >=1.21.1 {
		@Override
		protected void extractListBackground(GuiGraphicsExtractor context) {

		}

		@Override
		protected void extractListSeparators(GuiGraphicsExtractor context) {

		}
		//?}

		public void setEntries(List<EditorConfigEntry> entries) {
			this.clearEntries();
			for (EditorConfigEntry entry : entries) {
				this.addEntry(new ConfigEntryWidget(entry));
			}
		}

		@Override
		public void setSelected(@Nullable ConfigEntryWidget entry) {
			super.setSelected(entry);
			ConfigSelectionScreen.this.confirmButton.active = entry != null;
			ConfigSelectionScreen.this.previewWidget.setConfig(entry == null ? null : getConfig(entry.entry));
		}

		@Nullable
		private ParticleConfig getConfig(EditorConfigEntry entry) {
			EditorConfigsIndex.LoadedConfig loaded = EditorConfigsIndex.load(entry);
			return loaded == null ? null : loaded.config();
		}
	}

	private class ConfigEntryWidget extends EditorListEntry<ConfigEntryWidget> {

		private final EditorConfigEntry entry;

		@Nullable
		private List<ITexture> textures;

		public ConfigEntryWidget(EditorConfigEntry entry) {
			this.entry = entry;
		}

		@Override
		protected void renderEntry(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getContentX();
			int y = this.getContentY();
			int height = this.getContentHeight();
			Font font = ConfigSelectionScreen.this.font;

			this.extractIcon(context, x, y, height);

			int textOffset = 2;
			int textX = x + height + textOffset;
			int textY = y + (height - font.lineHeight * 2 - textOffset) / 2;
			int textWidth = this.getContentRight() - textX;

			context.text(font, trim(font, Component.literal(this.entry.getShortPath()), textWidth), textX, textY, -1);
			context.text(font, trim(font, this.getSubtitle(), textWidth), textX, textY + font.lineHeight + textOffset, -6250336);

			if (hovered) {
				CursorTooltipPositioner.render(context, List.of(Component.literal(this.entry.getFullPath())), mouseX, mouseY);
			}
		}

		private void extractIcon(GuiGraphicsExtractor context, int x, int y, int size) {
			List<ITexture> textures = this.getTextures();
			if (textures.isEmpty()) {
				return;
			}

			long iconPeriod = 1000L;
			ITexture texture = textures.get((int) (Util.getMillis() / iconPeriod % textures.size()));
			texture.initialize();

			DrawContextExtension.push(context);
			DrawContextExtension.translate(context, x, y, 0F);
			texture.render(context, 0, 0, size, size, -1);
			DrawContextExtension.pop(context);
			OptimizedDrawer.endDrawing();
		}

		private List<ITexture> getTextures() {
			if (this.textures == null) {
				this.textures = EditorConfigsIndex.loadTextures(this.entry);
			}
			return this.textures;
		}

		private static Component trim(Font font, Component text, int width) {
			String ellipsis = "...";
			if (font.width(text) <= width) {
				return text;
			}
			return Component.literal(font.plainSubstrByWidth(text.getString(), width - font.width(ellipsis)) + ellipsis);
		}

		private Component getSubtitle() {
			return InventoryParticles.text("editor.selection.source.%s".formatted(this.entry.source().getName()), this.entry.itemIds().size());
		}

		@Override
		public Component getNarration() {
			return Component.literal(this.entry.getFullPath());
		}
	}
}
