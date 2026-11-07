package net.lopymine.ip.editor.field;

import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class CycleRowWidget extends FieldRowWidget {

	private final List<Option> options;
	private final Button button;

	private int index;

	public CycleRowWidget(int indent, @Nullable GroupRowWidget group, Component label, List<Option> options, int index, Runnable onChange) {
		super(indent, group, label);
		this.options = options;
		this.index = Math.max(0, index);

		this.button = Button.builder(this.getCurrent().title(), (ignored) -> this.cycle(onChange))
				.bounds(0, 0, 100, 16)
				.build();

		this.updateTooltip();
		this.addWidget(this.button);
	}

	private void cycle(Runnable onChange) {
		this.index = (this.index + 1) % this.options.size();
		this.button.setMessage(this.getCurrent().title());
		this.updateTooltip();
		this.getCurrent().action().run();
		onChange.run();
	}

	private Option getCurrent() {
		return this.options.get(Math.min(this.index, this.options.size() - 1));
	}

	private void updateTooltip() {
		Component description = this.getCurrent().description();
		this.setTooltip(this.button, description == null ? List.of() : List.of(description));
	}

	@Override
	protected void updateDisabledState() {
		this.button.active = !this.isDisabled();
	}

	@Override
	protected int getValueWidth() {
		return MIN_VALUE_WIDTH;
	}

	@Override
	protected void layoutWidgets(int x, int y, int width) {
		int right = this.getContentRight();
		int boxX = this.getValueX();

		this.button.setX(boxX);
		this.button.setY(y + 2);
		this.button.setWidth(Math.max(40, right - boxX));
	}

	public record Option(Component title, @Nullable Component description, Runnable action) { }
}
