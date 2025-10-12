package net.lopymine.ip.gui.list;

import java.util.*;
import org.jetbrains.annotations.NotNull;

public class ResourcePackEntriesListWidget extends AbstractSearchListWidget<ResourcePackEntryWidget> {

	public ResourcePackEntriesListWidget(int x, int y, int width, int height, int buttonHeight) {
		super(x, y, width + 2 + 6 + 2, height, buttonHeight);
	}

	@Override
	public int addEntry(ResourcePackEntryWidget entry) {
		return super.addEntry(entry);
	}

	@Override
	public int getRowWidth() {
		return this.width - (2 + 6 + 2) - (5 * 2);
	}

	@Override
	public int getRowLeft() {
		return this.getX() + ((this.width - (2 + 6 + 2)) / 2) - (this.getRowWidth() / 2);
	}

	@Override
	protected boolean searched(String string, ResourcePackEntryWidget child) {
		return child.getWidget().getMessage().toString().contains(string);
	}

	@Override
	protected @NotNull Comparator<ResourcePackEntryWidget> getComparator() {
		return Comparator.comparing(a -> a.getWidget().getMessage().getString());
	}
}
