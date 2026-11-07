package net.lopymine.ip.editor.field;

import lombok.*;
import net.lopymine.ip.config.particle.ParticleHolder;
import net.lopymine.ip.editor.EditorItems;
import net.lopymine.ip.element.texture.ITexture;
import net.lopymine.ip.renderer.OptimizedDrawer;
import net.lopymine.ip.utils.ItemStackUtils;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.11 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Getter
public class GroupRowWidget extends FieldRowWidget {

	private static final int CHEVRON_WIDTH = 10;
	private final String key;
	@Setter
	private boolean expanded;

	@Nullable
	@Setter
	private RowIcon icon;

	@Setter
	private Runnable expandAction = () -> { };

	public GroupRowWidget(int indent, @Nullable GroupRowWidget group, Component label, String key, boolean expanded) {
		super(indent, group, label);
		this.key = key;
		this.expanded = expanded;
	}

	public void addPermanentAction(@Nullable FieldAction action) {
		this.addAction(action, true);
	}

	@Override
	protected int getLabelWidth() {
		return super.getLabelWidth() + CHEVRON_WIDTH;
	}

	@Override
	protected void layoutWidgets(int x, int y, int width) {

	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.extractRowHighlight(context, mouseX, mouseY);
		this.extractDepthLines(context);

		int color = getDepthColor(this.indent);
		extractChevron(context, this.getContentX(), this.getY() + 7, this.expanded, color);
		context.text(Minecraft.getInstance().font, this.label, this.getContentX() + CHEVRON_WIDTH, this.getY() + 6, this.getTextColor());

		if (this.icon != null) {
			this.icon.render(context, this.getContentX() + this.getLabelWidth() + 4, this.getY() + 2);
		}

		this.extractSubWidgets(context, mouseX, mouseY, deltaTicks);
	}

	public static RowIcon itemIcon(ParticleHolder holder) {
		return (context, x, y) -> holder.getItemOrTag().ifLeft((cachedItem) -> {
			if (EditorItems.isUnknown(cachedItem)) {
				EditorItems.renderUnknown(context, x, y);
				return;
			}
			context.item(ItemStackUtils.create(cachedItem.getItem()), x, y);
		});
	}

	public static RowIcon textureIcon(ITexture texture) {
		int iconSize = 16;
		return (context, x, y) -> {
			texture.initialize();
			DrawContextExtension.push(context);
			DrawContextExtension.translate(context, x, y, 0F);
			texture.render(context, 0, 0, iconSize, iconSize, -1);
			DrawContextExtension.pop(context);
			OptimizedDrawer.endDrawing();
		};
	}

	public interface RowIcon {

		void render(GuiGraphicsExtractor context, int x, int y);

	}

	//? if >=1.21.11 {
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (super.mouseClicked(event, doubled)) {
	//?} else {
	/*@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
	*///?}
			return true;
		}
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		this.expanded = !this.expanded;
		this.expandAction.run();
		return true;
	}
}
