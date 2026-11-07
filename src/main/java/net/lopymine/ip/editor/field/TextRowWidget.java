package net.lopymine.ip.editor.field;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class TextRowWidget extends FieldRowWidget {

	static final int BOX_HEIGHT = 16;

	private final FieldAccess access;
	private final Runnable onChange;

	private final EditBox editBox;
	@Nullable
	private final NumberScrollWidget scrollWidget;

	public TextRowWidget(int indent, @Nullable GroupRowWidget group, Component label, FieldAccess access, Runnable onChange) {
		super(indent, group, label);
		this.access = access;
		this.onChange = onChange;

		this.editBox = createBox(label, FieldValues.toText(access.type(), access.get()), this::onInput);
		this.scrollWidget = this.createScrollWidget(access, this.editBox);
		this.addWidget(this.editBox);
	}

	static EditBox createBox(Component label, String value, Consumer<String> responder) {
		EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 100, BOX_HEIGHT, label);
		box.setMaxLength(512);
		box.setTextColor(TEXT_COLOR);
		box.setValue(value);
		box.setResponder(responder);
		return box;
	}

	static void resetTextScroll(EditBox box) {
		if (box.isFocused()) {
			return;
		}
		box.setCursorPosition(0);
		box.setHighlightPos(0);
	}

	@Override
	protected void updateDisabledState() {
		setBoxEnabled(this.editBox, !this.isDisabled());

		if (this.scrollWidget != null) {
			this.scrollWidget.active = !this.isDisabled();
		}
	}

	static void setBoxEnabled(EditBox box, boolean enabled) {
		box.setEditable(enabled);
		box.active = enabled;
		if (!enabled) {
			box.setFocused(false);
			box.setHighlightPos(box.getCursorPosition());
		}
	}

	private void onInput(String text) {
		if (this.isDisabled()) {
			return;
		}

		Object value = FieldValues.fromText(this.access.type(), text, this.access.get());
		if (value == null) {
			this.editBox.setTextColor(INVALID_TEXT_COLOR);
			return;
		}

		this.editBox.setTextColor(TEXT_COLOR);
		this.access.set(value);
		this.onChange.run();
	}

	@Override
	protected int getValueWidth() {
		return MIN_VALUE_WIDTH + getScrollWidgetOffset(this.scrollWidget);
	}

	@Override
	protected void layoutWidgets(int x, int y, int width) {
		layoutBox(this.editBox, this.scrollWidget, this.getValueX(), y, this.getContentRight());
	}

	static int getScrollWidgetOffset(@Nullable NumberScrollWidget scrollWidget) {
		return scrollWidget == null ? 0 : NumberScrollWidget.WIDTH + 2;
	}

	static void layoutBox(EditBox box, @Nullable NumberScrollWidget scrollWidget, int x, int y, int right) {
		int offset = getScrollWidgetOffset(scrollWidget);

		if (scrollWidget != null) {
			scrollWidget.setX(x);
			scrollWidget.setY(y + 2);
		}

		box.setX(x + offset);
		box.setY(y + 2);
		box.setWidth(Math.max(20, right - x - offset));
		resetTextScroll(box);
	}
}
