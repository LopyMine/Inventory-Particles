package net.lopymine.ip.editor.field;

import com.mojang.blaze3d.platform.InputConstants;
//? if >=1.21.11 {
import com.mojang.blaze3d.platform.cursor.CursorTypes;
//?}
import java.util.*;
import lombok.Getter;
import net.lopymine.ip.utils.*;
import net.lopymine.ip.yacl.screen.ClippedPanelWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

public class FieldsPanelWidget extends ClippedPanelWidget {

	private static final int DEAD_ZONE = 8;
	private static final double SCROLL_SPEED = 0.08D;
	private static final double SCROLL_PROGRESSION = 1.35D;
	private static final double MAX_SCROLL_SPEED = 150.0D;

	private final Set<String> collapsedGroups = new HashSet<>();
	private final Set<String> expandedGroups = new HashSet<>();
	private final List<FieldRowWidget> rows = new ArrayList<>();

	@Getter
	private int contentHeight;
	private int contentWidth;

	private boolean autoScrolling;
	private int anchorY;
	private double scrollResidual;

	public FieldsPanelWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public boolean isCollapsed(String key) {
		return this.collapsedGroups.contains(key);
	}

	public boolean isExpanded(String key) {
		return this.expandedGroups.contains(key);
	}

	public void toggleGroup(String key, boolean expanded) {
		if (expanded) {
			this.collapsedGroups.remove(key);
			this.expandedGroups.add(key);
		} else {
			this.collapsedGroups.add(key);
			this.expandedGroups.remove(key);
		}
	}

	public void setRows(List<FieldRowWidget> rows) {
		this.setFocused((GuiEventListener) null);
		this.rows.clear();
		this.rows.addAll(rows);
		this.widgets.clear();
		this.widgets.addAll(rows);
		this.layoutRows();
	}

	public void layoutRows() {
		int base = this.getWidth() - 8;

		int height = 2;
		Map<GroupRowWidget, Integer> columns = new IdentityHashMap<>();

		for (FieldRowWidget row : this.rows) {
			boolean visible = row.isRowVisible();
			row.visible = visible;
			row.active = visible;
			if (!visible) {
				continue;
			}

			height += FieldRowWidget.ROW_HEIGHT + FieldRowWidget.ROW_OFFSET;
			if (row.getValueWidth() > 0) {
				columns.merge(row.getGroup(), row.getLabelColumnWidth(), Math::max);
			}
		}

		Map<GroupRowWidget, Integer> widths = new IdentityHashMap<>();
		for (FieldRowWidget row : this.rows) {
			if (row.visible) {
				widths.merge(row.getGroup(), row.getPreferredWidth(this.getValueColumn(columns, row)), Math::max);
			}
		}

		int width = base;
		for (int groupWidth : widths.values()) {
			width = Math.max(width, groupWidth);
		}

		this.contentHeight = height;
		this.contentWidth = width + 4;
		this.updateClip();

		int x = this.getX() + 2 + this.getClipX();
		int y = this.getY() + 2 + this.getClipY();

		int offset = 2;
		for (FieldRowWidget row : this.rows) {
			if (!row.visible) {
				continue;
			}

			row.layoutRow(x, y + offset - 2, Math.max(base, widths.get(row.getGroup())), this.getValueColumn(columns, row));
			offset += FieldRowWidget.ROW_HEIGHT + FieldRowWidget.ROW_OFFSET;
		}
	}

	private int getValueColumn(Map<GroupRowWidget, Integer> columns, FieldRowWidget row) {
		return Math.max((this.getWidth() - 8) / 2, columns.getOrDefault(row.getGroup(), 0));
	}

	@Override
	protected void onClipChanged() {
		this.layoutRows();
	}

	public void setAutoScrolling(boolean autoScrolling) {
		this.autoScrolling = autoScrolling;
		this.scrollResidual = 0.0D;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (!focused) {
			this.setAutoScrolling(false);
		}
	}

	@Override
	protected boolean rootMouseClicked(double mouseX, double mouseY, int button) {
		if (super.rootMouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (this.autoScrolling) {
			this.setAutoScrolling(false);
			return true;
		}
		if (button != InputConstants.MOUSE_BUTTON_MIDDLE) {
			return false;
		}

		this.anchorY = (int) mouseY;
		this.setAutoScrolling(true);
		return true;
	}

	@Override
	protected boolean rootMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (FieldRowWidget row : this.rows) {
			if (row.visible && !this.isOutOfView(row) && row.isScrollWidgetHovered(mouseX, mouseY)) {
				return false;
			}
		}
		return super.rootMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	protected boolean rootMouseReleased(double mouseX, double mouseY, int button) {
		if (this.autoScrolling && button == InputConstants.MOUSE_BUTTON_MIDDLE && Math.abs(mouseY - this.anchorY) > DEAD_ZONE) {
			this.setAutoScrolling(false);
			return true;
		}
		return super.rootMouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.tickAutoScroll(mouseY);
		super.extractWidgetRenderState(context, mouseX, mouseY, deltaTicks);

		//? if >=1.21.11 {
		if (this.autoScrolling) {
			context.requestCursor(CursorTypes.RESIZE_NS);
		}
		//?}
	}

	private void tickAutoScroll(int mouseY) {
		if (!this.autoScrolling) {
			return;
		}

		int distance = mouseY - this.anchorY;
		int magnitude = Math.abs(distance) - DEAD_ZONE;
		if (magnitude <= 0) {
			return;
		}

		float frameTicks = TickUtils.getRealtimeDeltaTicks();
		double speed = Math.min(Math.pow(magnitude, SCROLL_PROGRESSION) * SCROLL_SPEED, MAX_SCROLL_SPEED);
		this.scrollResidual += Math.signum(distance) * speed * frameTicks;

		int pixels = (int) this.scrollResidual;
		if (pixels == 0) {
			return;
		}

		this.scrollResidual -= pixels;
		this.moveClip(0, -pixels);
	}

	@Nullable
	public FieldRowWidget findRow(String key) {
		for (FieldRowWidget row : this.rows) {
			if (row instanceof GroupRowWidget group && group.getKey().equals(key)) {
				return group;
			}
		}
		return null;
	}

	public void scrollToRow(String key) {
		FieldRowWidget row = this.findRow(key);
		if (row == null) {
			return;
		}

		this.expandParents(row);
		this.layoutRows();
		this.scrollTo(row);
	}

	private void expandParents(FieldRowWidget row) {
		GroupRowWidget group = row.getGroup();
		while (group != null) {
			this.toggleGroup(group.getKey(), true);
			group.setExpanded(true);
			group = group.getGroup();
		}
	}

	@Override
	public int getMaxHeight() {
		return this.contentHeight;
	}

	@Override
	public int getMaxWidth() {
		return this.contentWidth;
	}
}
