package net.lopymine.ip.yacl.screen;

import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.element.mod.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.mossylib.gui.*;
import net.lopymine.mossylib.yacl.custom.MossyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

public class ConfigEditorMenu extends Screen implements MossyScreen {

	private Area listArea;
	private Area showcaseArea;
	private ItemStack currentItem = ItemStack.EMPTY;

	public ConfigEditorMenu() {
		super(Component.literal(""));
	}

	@Override
	protected void init() {
		int offset = 10;

		int listWidth = (this.width / 5);
		int listHeight = this.height - offset * 2;
		int scrollBarWidth = 2 + 6 + 2;
		this.listArea = new Area(this.width - listWidth - offset - scrollBarWidth, 10, listWidth, listHeight);

		EditBox searchWidget = new EditBox(Minecraft.getInstance().font, this.listArea.getX(), this.listArea.getY(), this.listArea.getWidth(), 20, Component.literal(""));
		searchWidget.setHint(Component.literal("Search"));
		this.addRenderableWidget(searchWidget);

		int a = searchWidget.getHeight() + offset;
		this.listArea.offsetY(a);
		this.listArea.expandHeight(-a);

		Area panelArea = new Area(offset, offset, (int) (this.listArea.getWidth() * 3F), listHeight);
		int showcaseHeight = panelArea.getHeight() / -3;
		panelArea.expandHeight(showcaseHeight - offset);
		this.showcaseArea = new Area(panelArea.getX(), panelArea.getBottom() + offset, panelArea.getWidth(), showcaseHeight * -1);

		ClippedPanelWidget panelWidget = new ClippedPanelWidget(panelArea.getX(), panelArea.getY(), panelArea.getWidth(), panelArea.getHeight());
		this.addRenderableWidget(panelWidget);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundRenderer.drawTransparencyBackground(context, this.listArea.getX(), this.listArea.getY(), this.listArea.getWidth(), this.listArea.getHeight(), true);
		BackgroundRenderer.drawTransparencyBackground(context, this.listArea.getRight() + 1, this.listArea.getY(), 2 + 6 + 2, this.listArea.getHeight(), true);
		BackgroundRenderer.drawTransparencyBackground(context, this.showcaseArea.getX(), this.showcaseArea.getY(), this.showcaseArea.getWidth(), this.showcaseArea.getHeight(), true);
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);

		context.enableScissor(this.showcaseArea.getX() + 2, this.showcaseArea.getY() + 2, this.showcaseArea.getRight() - 2, this.showcaseArea.getBottom() - 2);

		boolean bl = this.showcaseArea.over(mouseX, mouseY);
		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		cursor.setMouseX(bl ? mouseX : this.showcaseArea.getCenterX());
		cursor.setMouseY(bl ? mouseY : this.showcaseArea.getCenterY());
		cursor.setStack(this.currentItem);
		context.item(this.currentItem, (int) cursor.getMouseX() - 8, (int) cursor.getMouseY() - 8);

		context.disableScissor();
	}

	@Override
	public void tick() {
		InventoryParticlesRenderer.getInstance().tick(null, null, null);
	}
}
