package net.lopymine.ip.gui.list;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget.Entry;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.*;

@Getter
@Setter
public abstract class AbstractSearchListWidget<E extends Entry<E>> extends AbstractVersionedEntryListWidget<E> {

	public static final MutableText NOTHING_FOUND_TEXT = InventoryParticles.text("text.nothing_found");

	private final List<E> searchWidgets = new ArrayList<>();
	private boolean searching = false;

	public AbstractSearchListWidget(int x, int y, int width, int height, int itemHeight) {
		super(x, y, width, height, itemHeight);
	}

	//? if >=1.21.9 {

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
		this.startScissor(context);
		for(E entry : this.getWidgets()) {
			if (entry.getY() + entry.getHeight() >= this.getY() && entry.getY() <= this.getBottom()) {
				this.renderEntry(context, mouseX, mouseY, delta, entry);
			}
		}
		this.endScissor(context);
	}

	public void setFocused(@Nullable Element focused) {
		E entry = this.getFocused();
		if (entry != focused && entry instanceof ParentElement parentElement) {
			parentElement.setFocused(null);
		}

		super.setFocused(focused);
		int i = this.getWidgets().indexOf(focused);
		if (i >= 0) {
			this.setSelected(this.getWidgets().get(i));
		}

	}

	protected List<E> getWidgets() {
		return this.searching ? this.searchWidgets : this.children();
	}

	//?} else {
	/*@Override
		protected ResourcePackEntryWidget getEntry(int index) {
		return this.searching ? this.searchWidgets.get(index) : super.getEntry(index);
	}
	*///?}

	@Override
	protected void drawMenuListBackground(DrawContext context) {
		if (this.searching && this.searchWidgets.isEmpty()) {
			int a = (this.getWidth() - this.getRowWidth()) / 2;
			ClickableWidget.drawScrollableText(context, MinecraftClient.getInstance().textRenderer, NOTHING_FOUND_TEXT, this.getX() + a, this.getY(), this.getX() + this.getWidth() - a, this.getY() + this.getHeight() + 4, -1);
		}
	}

	@Override
	protected int getEntryCount() {
		return this.searching ? this.searchWidgets.size() : super.getEntryCount();
	}

	public void search(String string) {
		this.setListScrollAmount(0);

		if (string.isEmpty()) {
			this.searching = false;
			return;
		}

		this.searchWidgets.clear();
		for (E child : this.children()) {
			if (searched(string, child)) {
				this.searchWidgets.add(child);
			}
		}

		this.searchWidgets.sort(getComparator());
		this.searching = true;
	}

	protected abstract @NotNull Comparator<E> getComparator();

	protected abstract boolean searched(String string, E child);

}
