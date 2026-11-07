package net.lopymine.ip.editor.screen;

import java.util.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.editor.EditorItems;
import net.lopymine.ip.editor.preview.EditorParticlesRenderer;
import net.lopymine.ip.utils.ItemStackUtils;
import net.lopymine.ip.yacl.screen.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class HoldersPreviewWidget extends ClippedPanelWidget {

	private static final int CELL_SIZE = 32;

	private final EditorParticlesRenderer renderer = new EditorParticlesRenderer();
	private final List<PreviewItem> previewItems = new ArrayList<>();

	@Nullable
	private ParticleConfig config;
	@Nullable
	private PreviewItem hoveredItem;

	public HoldersPreviewWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public void setConfig(@Nullable ParticleConfig config) {
		this.config = config;
		this.previewItems.clear();
		this.resetClip();
		this.hoveredItem = null;
		this.renderer.setSource(null, List.of());

		if (config == null) {
			return;
		}

		List<ParticleHolder> holders = new ArrayList<>(config.getHolders());
		holders.sort(Comparator.comparing(ParticleHolder::getName));

		int columns = Math.max(1, (this.getWidth() - 8) / CELL_SIZE);
		for (int i = 0; i < holders.size(); i++) {
			ParticleHolder holder = holders.get(i);
			CachedItem cachedItem = holder.getItemOrTag().left().orElse(null);
			if (cachedItem == null) {
				continue;
			}

			int x = 4 + i % columns * CELL_SIZE;
			int y = 4 + i / columns * CELL_SIZE;
			boolean unknown = EditorItems.isUnknown(cachedItem);
			this.previewItems.add(new PreviewItem(holder, cachedItem, unknown ? ItemStack.EMPTY : ItemStackUtils.create(cachedItem.getItem()), x, y));
		}
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractWidgetRenderState(context, mouseX, mouseY, deltaTicks);

		context.enableScissor(this.getX() + 2, this.getY() + 2, this.getX() + this.getWidth() - 2, this.getY() + this.getHeight() - 2);

		PreviewItem hovered = null;
		for (PreviewItem previewItem : this.previewItems) {
			int x = (int) previewItem.screenX(this) - 8;
			int y = (int) previewItem.screenY(this) - 8;
			if (y + 16 < this.getY() || y > this.getY() + this.getHeight()) {
				continue;
			}

			if (previewItem.stack().isEmpty()) {
				EditorItems.renderUnknown(context, x, y);
			} else {
				context.item(previewItem.stack(), x, y);
			}

			if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
				hovered = previewItem;
			}
		}

		if (hovered != this.hoveredItem) {
			this.hoveredItem = hovered;
			this.renderer.setSource(this.config, hovered == null ? List.of() : List.of(hovered.holder()));
		}

		if (this.hoveredItem != null) {
			this.renderer.advance(this.hoveredItem.screenX(this), this.hoveredItem.screenY(this), this.hoveredItem.stack());
		}

		this.renderer.render(context);
		context.disableScissor();

		if (this.hoveredItem != null && this.hoveredItem.stack().isEmpty()) {
			CursorTooltipPositioner.render(context, EditorItems.getUnknownTooltip(this.hoveredItem.item().getId()), mouseX, mouseY);
		}
	}

	@Override
	public int getMaxHeight() {
		int max = 0;
		for (PreviewItem previewItem : this.previewItems) {
			max = Math.max(max, previewItem.y() + CELL_SIZE);
		}
		return max;
	}

	@Override
	public int getMaxWidth() {
		return this.getWidth();
	}

	private record PreviewItem(ParticleHolder holder, CachedItem item, ItemStack stack, int x, int y) {

		private double screenX(HoldersPreviewWidget panel) {
			return panel.getX() + 2 + panel.getClipX() + this.x + 8;
		}

		private double screenY(HoldersPreviewWidget panel) {
			return panel.getY() + 2 + panel.getClipY() + this.y + 8;
		}
	}
}
