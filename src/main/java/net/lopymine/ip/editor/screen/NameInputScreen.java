package net.lopymine.ip.editor.screen;

import java.util.function.*;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;

public class NameInputScreen extends EditorScreen {

	private static final int WIDGET_WIDTH = 220;
	private static final int WIDGET_HEIGHT = 20;
	private static final int WIDGET_OFFSET = 4;

	@Nullable
	private final Screen parent;
	private final Component hint;
	private final Component confirmText;
	private final String initialValue;
	private final Predicate<String> validator;
	private final Consumer<String> onConfirm;
	@Nullable
	private final Function<String, Component> subtitle;

	private EditBox nameWidget;
	private Button confirmButton;

	public NameInputScreen(
			@Nullable Screen parent,
			Component title,
			Component hint,
			Component confirmText,
			String initialValue,
			Predicate<String> validator,
			Consumer<String> onConfirm,
			@Nullable Function<String, Component> subtitle
	) {
		super(title);
		this.parent = parent;
		this.hint = hint;
		this.confirmText = confirmText;
		this.initialValue = initialValue;
		this.validator = validator;
		this.onConfirm = onConfirm;
		this.subtitle = subtitle;
	}

	@Override
	protected void init() {
		int x = (this.width - WIDGET_WIDTH) / 2;
		int y = this.height / 2 - WIDGET_HEIGHT;

		this.nameWidget = new EditBox(this.font, x, y, WIDGET_WIDTH, WIDGET_HEIGHT, this.hint);
		this.nameWidget.setMaxLength(128);
		this.nameWidget.setHint(this.hint.copy().withStyle(ChatFormatting.WHITE));
		this.nameWidget.setValue(this.initialValue);
		this.nameWidget.setResponder((value) -> this.confirmButton.active = this.validator.test(value.trim()));
		this.addRenderableWidget(this.nameWidget);

		y += WIDGET_HEIGHT + WIDGET_OFFSET * 4;
		this.confirmButton = this.addRenderableWidget(Button.builder(this.confirmText, (button) -> this.confirm())
				.bounds(x, y, WIDGET_WIDTH, WIDGET_HEIGHT)
				.build());
		this.confirmButton.active = this.validator.test(this.initialValue.trim());

		y += WIDGET_HEIGHT + WIDGET_OFFSET;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose())
				.bounds(x, y, WIDGET_WIDTH, WIDGET_HEIGHT)
				.build());

		this.setInitialFocus(this.nameWidget);
	}

	private void confirm() {
		String value = this.nameWidget.getValue().trim();
		if (!this.validator.test(value)) {
			return;
		}
		this.onConfirm.accept(value);
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.renderScreen(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, this.height / 4, -1);

		if (this.subtitle != null) {
			Component component = this.subtitle.apply(this.nameWidget.getValue().trim());
			context.centeredText(this.font, component, this.width / 2, this.nameWidget.getY() - WIDGET_OFFSET - this.font.lineHeight, -6250336);
		}
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}
}
