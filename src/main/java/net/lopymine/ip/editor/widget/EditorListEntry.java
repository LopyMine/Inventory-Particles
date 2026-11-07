package net.lopymine.ip.editor.widget;

//? if <1.21.11 {
/*import com.mojang.blaze3d.platform.InputConstants;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
*///?}
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;

public abstract class EditorListEntry<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList.Entry<E> {

	//? if <1.21.11 {
	/*@Setter
	@Nullable
	private EditorList<E> parentList;
	*///?}

	//? if <1.21.11 {
	/*private int contentX;
	private int contentY;
	private int contentWidth;
	private int contentHeight;
	*///?}

	protected abstract void renderEntry(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks);

	//? if >=26.1 {
	@Override
	public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		this.renderEntry(context, mouseX, mouseY, hovered, deltaTicks);
	}
	//?} elif >=1.21.11 {
	/*@Override
	public void renderContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		this.renderEntry(context, mouseX, mouseY, hovered, deltaTicks);
	}
	*///?} else {
	/*@Override
	public void render(GuiGraphicsExtractor context, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		this.contentX      = left;
		this.contentY      = top;
		this.contentWidth  = width;
		this.contentHeight = height;
		this.renderEntry(context, mouseX, mouseY, hovered, deltaTicks);
	}

	public int getContentX() {
		return this.contentX;
	}

	public int getContentY() {
		return this.contentY;
	}

	public int getContentWidth() {
		return this.contentWidth;
	}

	public int getContentHeight() {
		return this.contentHeight;
	}

	public int getContentRight() {
		return this.contentX + this.contentWidth;
	}
	*///?}

	//? if <1.21.11 {
	/*@Override
	@SuppressWarnings("unchecked")
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != InputConstants.MOUSE_BUTTON_LEFT || this.parentList == null) {
			return false;
		}

		this.parentList.setSelected((E) this);
		return true;
	}
	*///?}
}
