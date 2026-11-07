package net.lopymine.ip.editor.field;

import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class RangeRowWidget extends FieldRowWidget {

	private final Runnable onChange;
	private final FieldAccess min;
	private final FieldAccess max;

	private final EditBox minBox;
	private final EditBox maxBox;
	@Nullable
	private final NumberScrollWidget minScrollWidget;
	@Nullable
	private final NumberScrollWidget maxScrollWidget;

	public RangeRowWidget(int indent, @Nullable GroupRowWidget group, Component label, FieldAccess min, FieldAccess max, Runnable onChange) {
		super(indent, group, label);
		this.onChange = onChange;
		this.min = min;
		this.max = max;

		this.minBox = this.createBox(min, "editor.field.min");
		this.minScrollWidget = this.createScrollWidget(min, this.minBox);
		this.maxBox = this.createBox(max, "editor.field.max");
		this.maxScrollWidget = this.createScrollWidget(max, this.maxBox);

		this.addWidget(this.minBox);
		this.addWidget(this.maxBox);
	}

	private EditBox createBox(FieldAccess access, String tooltipKey) {
		EditBox box = TextRowWidget.createBox(this.label, FieldValues.toText(access.type(), access.get()), (text) -> this.onInput(access, text));
		box.setMaxLength(32);
		this.setTooltip(box, List.of(InventoryParticles.text(tooltipKey)));
		return box;
	}

	private void onInput(FieldAccess access, String text) {
		if (this.isDisabled()) {
			return;
		}

		Object value = FieldValues.fromText(access.type(), text, access.get());
		if (value == null) {
			return;
		}

		access.set(value);

		if (access == this.min) {
			this.raiseMax();
		}
		if (access == this.max) {
			this.lowerMin();
		}

		this.onChange.run();
	}

	private void raiseMax() {
		if (!(this.min.get() instanceof Number minValue) || !(this.max.get() instanceof Number maxValue) || minValue.doubleValue() <= maxValue.doubleValue()) {
			return;
		}

		Object value = FieldValues.asNumber(this.max.type(), minValue.doubleValue());
		if (value == null) {
			return;
		}

		this.max.set(value);
		this.maxBox.setValue(FieldValues.toText(this.max.type(), value));
	}

	private void lowerMin() {
		if (!(this.min.get() instanceof Number minValue) || !(this.max.get() instanceof Number maxValue) || maxValue.doubleValue() >= minValue.doubleValue()) {
			return;
		}

		Object value = FieldValues.asNumber(this.min.type(), maxValue.doubleValue());
		if (value == null) {
			return;
		}

		this.min.set(value);
		this.minBox.setValue(FieldValues.toText(this.min.type(), value));
	}

	@Override
	protected void updateDisabledState() {
		TextRowWidget.setBoxEnabled(this.minBox, !this.isDisabled());
		TextRowWidget.setBoxEnabled(this.maxBox, !this.isDisabled());

		if (this.minScrollWidget != null) {
			this.minScrollWidget.active = !this.isDisabled();
		}
		if (this.maxScrollWidget != null) {
			this.maxScrollWidget.active = !this.isDisabled();
		}
	}

	@Override
	protected int getValueWidth() {
		int minBoxWidth = 70;
		return (minBoxWidth + TextRowWidget.getScrollWidgetOffset(this.minScrollWidget)) * 2 + 4;
	}

	@Override
	protected void layoutWidgets(int x, int y, int width) {
		int right = this.getContentRight();
		int boxX = this.getValueX();
		int half = (right - boxX - 4) / 2;

		TextRowWidget.layoutBox(this.minBox, this.minScrollWidget, boxX, y, boxX + half);
		TextRowWidget.layoutBox(this.maxBox, this.maxScrollWidget, boxX + half + 4, y, right);
	}
}
