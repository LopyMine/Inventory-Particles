package net.lopymine.ip.editor.field;

import java.util.*;
import lombok.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.editor.EditorTooltips;
import net.lopymine.ip.utils.WidgetUtils;
import net.lopymine.ip.yacl.screen.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class FieldRowWidget extends AbstractSubWidgetsWidget<AbstractWidget> implements ClippedPanelWidget.TooltipProvider {

	public static final int ROW_HEIGHT = 20;
	public static final int ROW_OFFSET = 2;
	public static final int INDENT_WIDTH = 10;
	public static final int ACTION_SIZE = 14;
	public static final int LABEL_GAP = 8;
	public static final int MIN_VALUE_WIDTH = 120;
	public static final int LABEL_COLOR = -1;
	public static final int INHERITED_COLOR = -6250336;
	public static final int HOVER_COLOR = 0x20FFFFFF;
	public static final int TEXT_COLOR = 0xFFE0E0E0;
	public static final int INVALID_TEXT_COLOR = 0xFFFF5555;

	private static final int[] DEPTH_COLORS = {
			0xFF7FB3FF,
			0xFF7FE6A0,
			0xFFFFD27F,
			0xFFFF9EB5,
			0xFFC79EFF,
			0xFF9EF0FF
	};

	protected final int indent;
	@Nullable
	protected final GroupRowWidget group;
	protected final Component label;

	private final Map<AbstractWidget, List<Component>> tooltips = new IdentityHashMap<>();
	private final List<Button> actions = new ArrayList<>();
	private final Set<Button> permanentActions = Collections.newSetFromMap(new IdentityHashMap<>());

	protected boolean inherited;
	protected boolean locked;

	private int labelWidth = -1;
	private int valueColumn;

	public FieldRowWidget(int indent, @Nullable GroupRowWidget group, Component label) {
		super(0, 0, 0, ROW_HEIGHT, label);
		this.indent = indent;
		this.group = group;
		this.label = label;
	}

	public boolean isRowVisible() {
		if (this.group == null) {
			return true;
		}
		return this.group.isExpanded() && this.group.isRowVisible();
	}

	public void addAction(@Nullable FieldAction action, boolean permanent) {
		if (action == null) {
			return;
		}

		Button button = Button.builder(Component.literal(action.icon()), (ignored) -> action.action().run())
				.bounds(0, 0, ACTION_SIZE, ACTION_SIZE)
				.build();
		button.visible = permanent;

		this.setTooltip(button, List.of(InventoryParticles.text(action.tooltipKey())));
		this.actions.add(button);
		if (permanent) {
			this.permanentActions.add(button);
		}
		this.addWidget(button);
	}

	public void addAction(@Nullable FieldAction action) {
		this.addAction(action, false);
	}

	@Nullable
	protected NumberScrollWidget createScrollWidget(FieldAccess access, EditBox box) {
		if (!FieldValues.isNumber(access.type())) {
			return null;
		}

		NumberScrollWidget widget = new NumberScrollWidget(access, box);
		this.setTooltip(widget, List.of(InventoryParticles.text("editor.field.scroll_hint")));
		this.addWidget(widget);
		return widget;
	}

	public int getLabelColumnWidth() {
		return this.indent * INDENT_WIDTH + 4 + this.getLabelWidth() + LABEL_GAP;
	}

	public int getPreferredWidth(int valueColumn) {
		return valueColumn + this.getValueWidth() + this.getActionsWidth() + 6;
	}

	protected int getLabelWidth() {
		if (this.labelWidth == -1) {
			this.labelWidth = Minecraft.getInstance().font.width(this.label);
		}
		return this.labelWidth;
	}

	protected int getValueWidth() {
		return 0;
	}

	protected int getContentRight() {
		return this.getX() + this.getWidth() - 6 - this.getActionsWidth();
	}

	protected int getValueX() {
		return this.getX() + this.valueColumn;
	}

	public boolean isScrollWidgetHovered(double mouseX, double mouseY) {
		for (AbstractWidget widget : this.widgets) {
			if (widget instanceof NumberScrollWidget scrollWidget && scrollWidget.visible && scrollWidget.isMouseOver(mouseX, mouseY)) {
				return true;
			}
		}
		return false;
	}

	public void layoutRow(int x, int y, int width, int valueColumn) {
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.valueColumn = valueColumn;
		this.layoutActions(y);
		this.layoutWidgets(this.getContentX(), y, width);
	}

	protected abstract void layoutWidgets(int x, int y, int width);

	protected void layoutActions(int y) {
		int x = this.getX() + this.getWidth() - ACTION_SIZE - 6;
		for (int i = this.actions.size() - 1; i >= 0; i--) {
			Button button = this.actions.get(i);
			button.setX(x);
			button.setY(y + 3);
			x -= ACTION_SIZE + 2;
		}
	}

	protected int getActionsWidth() {
		return this.actions.size() * (ACTION_SIZE + 2);
	}

	protected int getContentX() {
		return this.getX() + this.indent * INDENT_WIDTH + 4;
	}

	protected int getTextColor() {
		return this.isDisabled() ? INHERITED_COLOR : LABEL_COLOR;
	}

	public boolean isDisabled() {
		return this.inherited || this.locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		this.updateDisabledState();
	}

	protected void updateDisabledState() {

	}

	protected static int getDepthColor(int depth) {
		return DEPTH_COLORS[Math.max(0, depth) % DEPTH_COLORS.length];
	}

	protected void setTooltip(AbstractWidget widget, List<Component> tooltip) {
		this.tooltips.put(widget, tooltip);
	}

	@Override
	@Nullable
	public List<Component> getClippedTooltip(int mouseX, int mouseY) {
		if (!EditorTooltips.isEnabled() || !this.isRowHovered(mouseX, mouseY)) {
			return null;
		}

		for (Map.Entry<AbstractWidget, List<Component>> entry : this.tooltips.entrySet()) {
			AbstractWidget widget = entry.getKey();
			if (!widget.visible || widget instanceof NumberScrollWidget scrollWidget && scrollWidget.isScrolling()) {
				continue;
			}
			if (mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth() && mouseY >= widget.getY() && mouseY <= widget.getY() + widget.getHeight()) {
				return entry.getValue();
			}
		}

		return null;
	}

	public void setInherited(boolean inherited) {
		this.inherited = inherited;
		this.updateDisabledState();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.extractRowHighlight(context, mouseX, mouseY);
		this.extractDepthLines(context);
		context.text(Minecraft.getInstance().font, this.label, this.getContentX(), this.getY() + 6, this.getTextColor());
		this.extractSubWidgets(context, mouseX, mouseY, deltaTicks);
	}

	protected void extractRowHighlight(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		if (!this.isRowHovered(mouseX, mouseY)) {
			return;
		}
		context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), HOVER_COLOR);
	}

	protected void extractSubWidgets(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		boolean hovered = this.isRowHovered(mouseX, mouseY);
		for (Button button : this.actions) {
			button.visible = hovered || this.permanentActions.contains(button);
		}

		for (AbstractWidget widget : this.widgets) {
			if (!widget.visible) {
				continue;
			}
			WidgetUtils.render(widget, context, mouseX, mouseY, deltaTicks);
		}
	}

	protected void extractDepthLines(GuiGraphicsExtractor context) {
		for (int depth = 0; depth < this.indent; depth++) {
			int x = this.getX() + depth * INDENT_WIDTH + 2;
			context.fill(x, this.getY(), x + 1, this.getY() + this.getHeight(), getDepthColor(depth));
		}
	}

	protected static void extractChevron(GuiGraphicsExtractor context, int x, int y, boolean expanded, int color) {
		if (expanded) {
			for (int i = 0; i < 3; i++) {
				context.fill(x + i, y + i, x + 7 - i, y + i + 1, color);
			}
			return;
		}
		for (int i = 0; i < 3; i++) {
			context.fill(x + i, y + i, x + i + 1, y + 7 - i, color);
		}
	}

	protected boolean isRowHovered(int mouseX, int mouseY) {
		return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight();
	}
}
