package net.lopymine.ip.gui.config;

import java.util.*;
import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.gui.list.AbstractSubWidgetsWidget;
import net.lopymine.ip.utils.ArgbUtils2;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ExtensionMethod(DrawContextExtension.class)
public class ConfigOptionEditorWidget<T> extends AbstractSubWidgetsWidget<TextFieldWidget> {

	private final List<OptionField<T, String>> fields = new ArrayList<>();
	@Nullable
	private T config;

	@SafeVarargs
	public ConfigOptionEditorWidget(int x, int y, int fieldWidth, Text text, OptionEditor<T, String>... editors) {
		super(x, y, fieldWidth * editors.length + ((editors.length - 1) * 2), (MinecraftClient.getInstance().textRenderer.fontHeight * 2) + 4 + 20, text);

		int xOffset = 2;
		for (OptionEditor<T, String> editor : editors) {
			TextFieldWidget field = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, xOffset, (MinecraftClient.getInstance().textRenderer.fontHeight + 2) * 2, fieldWidth, 20, Text.of(""));
			field.setChangedListener((s) -> {
				if (this.config == null) {
					return;
				}
				try {
					editor.set(this.config, s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			this.addWidget(field);
			this.fields.add(new OptionField<>(field, editor));
			xOffset += fieldWidth + 2;
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.push();
		context.translate(this.getX(), this.getY(), 0);
		context.fill(0, 0, 1, this.getHeight(), ArgbUtils2.getArgb(255, 255, 255, 255));
		DrawUtils.drawText(context, 4, 2, this.getWidth() - 2, this.getMessage());
		for (OptionField<T, String> field : this.fields) {
			TextFieldWidget widget = field.widget();
			DrawUtils.drawText(context, widget.getX() + 2, widget.getY() - MinecraftClient.getInstance().textRenderer.fontHeight, widget.getWidth(), field.editor().getName());
			widget.renderWidget(context, mouseX - this.getX() - widget.getX(), mouseY - this.getY() - widget.getY(), deltaTicks);
		}
		context.pop();
	}

	public void setConfig(@Nullable T config) {
		this.config = config;
		for (OptionField<T, String> field : this.fields) {
			field.widget().setText(field.editor().get(config));
		}
	}

	public record OptionField<A, B>(TextFieldWidget widget, OptionEditor<A, B> editor) {}

	public interface OptionEditor<A, B> {

		void set(A config, B value);

		B get(A config);

		Text getName();

	}

}
