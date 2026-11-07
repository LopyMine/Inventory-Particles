package net.lopymine.ip.editor.widget;

//? if <1.21.1 {
/*import net.lopymine.mossylib.gui.BackgroundRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

public abstract class EditorList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {

	//? if <1.21.1 {
	/*private static final int SCROLLBAR_WIDTH = 6;

	private boolean chrome = true;
	*///?}

	public EditorList(Minecraft minecraft, int x, int y, int width, int height, int entryHeight) {
		//? if >=1.21.1 {
		super(minecraft, width, height, y, entryHeight);
		this.setX(x);
		//?} else {
		/*super(minecraft, width, height, y, y + height, entryHeight);
		this.setLeftPos(x);
		*///?}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected int addEntry(E entry) {
		//? if <1.21.11 {
		/*if (entry instanceof EditorListEntry<?> editorEntry) {
			((EditorListEntry<E>) editorEntry).setParentList(this);
		}
		*///?}
		return super.addEntry(entry);
	}

	protected void disableChrome() {
		//? if <1.21.1 {
		/*this.chrome = false;
		this.setRenderBackground(false);
		this.setRenderTopAndBottom(false);
		*///?}
	}

	//? if <1.21.1 {
	/*@Override
	protected int getScrollbarPosition() {
		return this.getX() + this.getWidth() - SCROLLBAR_WIDTH;
	}

	@Override
	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		if (this.chrome) {
			BackgroundRenderer.drawTransparencyBackground(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), true);
		}
		super.render(context, mouseX, mouseY, deltaTicks);
	}

	public int getX() {
		return this.x0;
	}

	public int getY() {
		return this.y0;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}
	*///?}
}
