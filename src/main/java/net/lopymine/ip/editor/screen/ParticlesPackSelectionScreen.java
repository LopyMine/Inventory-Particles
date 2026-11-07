package net.lopymine.ip.editor.screen;

import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.editor.*;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.mossylib.utils.DrawUtils;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;

public class ParticlesPackSelectionScreen extends EditorScreen {

	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_OFFSET = 4;

	private static final int ENTRY_HEIGHT = 36;

	private static final int LEGACY_COLOR = 0xFFFFA000;
	private static final int DESCRIPTION_COLOR = -6250336;

	@Nullable
	private final Screen parent;

	private PacksListWidget listWidget;
	private Button editButton;
	private Button openButton;

	public ParticlesPackSelectionScreen(@Nullable Screen parent) {
		super(InventoryParticles.text("editor.selection.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int listBottom = this.height - BUTTON_HEIGHT - BUTTON_OFFSET * 2;

		ParticlesPacksManager.reloadPacks();
		List<ParticlesPackInfo> packs = ParticlesPacksManager.getEditablePacks();

		this.listWidget = new PacksListWidget(this.minecraft, 0, 32, this.width, listBottom - 32);
		this.listWidget.setPacks(packs);
		this.addRenderableWidget(this.listWidget);

		ParticlesPacksManager.scanAsync(packs, this::onScanned);

		int buttons = 4;
		int y = listBottom + BUTTON_OFFSET;
		int buttonWidth = Math.min(BUTTON_WIDTH, (this.width - BUTTON_OFFSET * (buttons + 1)) / buttons);
		int x = (this.width - buttonWidth * buttons - BUTTON_OFFSET * (buttons - 1)) / 2;

		this.editButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.selection.edit"), (button) -> this.edit())
				.bounds(x, y, buttonWidth, BUTTON_HEIGHT)
				.build());
		this.editButton.active = false;

		this.openButton = this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.selection.open"), (button) -> this.open())
				.bounds(x + buttonWidth + BUTTON_OFFSET, y, buttonWidth, BUTTON_HEIGHT)
				.build());
		this.openButton.active = false;

		this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.selection.refresh"), (button) -> this.refresh())
				.bounds(x + (buttonWidth + BUTTON_OFFSET) * 2, y, buttonWidth, BUTTON_HEIGHT)
				.build());

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose())
				.bounds(x + (buttonWidth + BUTTON_OFFSET) * 3, y, buttonWidth, BUTTON_HEIGHT)
				.build());
	}

	private void open() {
		PackEntryWidget selected = this.listWidget.getSelected();
		if (selected == null) {
			return;
		}
		ParticlesPacksManager.openInExplorer(selected.info);
	}

	private void refresh() {
		ParticlesPacksManager.clearScannedContents();
		this.rebuildWidgets();
	}

	private void onScanned(ParticlesPackInfo info) {
		if (ScreenUtils.current() != this) {
			return;
		}

		for (PackEntryWidget entry : this.listWidget.children()) {
			if (entry.info.packId().equals(info.packId())) {
				entry.info = info;
				return;
			}
		}
	}

	private void edit() {
		PackEntryWidget selected = this.listWidget.getSelected();
		if (selected == null) {
			return;
		}
		PackParticlesScreen.open(this, ParticlesPacksManager.scan(selected.info));
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.renderScreen(context, mouseX, mouseY, deltaTicks);
		context.centeredText(this.font, this.title, this.width / 2, 14, -1);

		if (this.listWidget.children().isEmpty()) {
			context.centeredText(this.font, InventoryParticles.text("editor.selection.no_packs"), this.width / 2, this.listWidget.getY() + 20, DESCRIPTION_COLOR);
		}
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}

	private class PacksListWidget extends EditorList<PackEntryWidget> {

		public PacksListWidget(Minecraft minecraft, int x, int y, int width, int height) {
			super(minecraft, x, y, width, height, ENTRY_HEIGHT);
		}

		public void setPacks(List<ParticlesPackInfo> packs) {
			this.clearEntries();
			for (ParticlesPackInfo pack : packs) {
				this.addEntry(new PackEntryWidget(pack));
			}
		}

		@Override
		public int getRowWidth() {
			int listWidth = 400;
			return Math.min(listWidth, this.width - 30);
		}

		@Override
		public void setSelected(@Nullable PackEntryWidget entry) {
			super.setSelected(entry);
			ParticlesPackSelectionScreen.this.editButton.active = entry != null;
			ParticlesPackSelectionScreen.this.openButton.active = entry != null;
		}
	}

	private class PackEntryWidget extends EditorListEntry<PackEntryWidget> {

		private ParticlesPackInfo info;

		public PackEntryWidget(ParticlesPackInfo info) {
			this.info = info;
		}

		@Override
		protected void renderEntry(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getContentX();
			int y = this.getContentY();
			int height = this.getContentHeight();
			boolean legacy = this.info.legacyLayout();

			if (legacy) {
				this.extractBorder(context, x - 1, y - 1, this.getContentWidth() + 2, height + 2, LEGACY_COLOR);
			}

			DrawUtils.drawTexture(context, ParticlesPacksManager.getIcon(this.info), x, y, 0, 0, height, height, height, height);

			Font font = ParticlesPackSelectionScreen.this.font;
			int textOffset = 2;
			int textX = x + height + textOffset;
			int textY = y + (height - font.lineHeight * 2 - textOffset) / 2;

			context.text(font, this.info.title(), textX, textY, legacy ? LEGACY_COLOR : -1);
			context.text(font, this.getDescription(), textX, textY + font.lineHeight + textOffset, legacy ? LEGACY_COLOR : DESCRIPTION_COLOR);
		}

		private void extractBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
			context.fill(x, y, x + width, y + 1, color);
			context.fill(x, y + height - 1, x + width, y + height, color);
			context.fill(x, y, x + 1, y + height, color);
			context.fill(x + width - 1, y, x + width, y + height, color);
		}

		private Component getDescription() {
			if (!this.info.isScanned()) {
				return InventoryParticles.text("editor.selection.entry.scanning");
			}
			if (this.info.legacyLayout()) {
				return InventoryParticles.text("editor.selection.entry.legacy_layout", this.info.particleConfigs(), this.info.familyConfigs());
			}
			if (this.info.hasConfigs()) {
				return InventoryParticles.text("editor.selection.entry.configs", this.info.particleConfigs(), this.info.familyConfigs());
			}
			return InventoryParticles.text("editor.selection.entry.empty");
		}

		@Override
		public Component getNarration() {
			return this.info.title();
		}
	}
}
