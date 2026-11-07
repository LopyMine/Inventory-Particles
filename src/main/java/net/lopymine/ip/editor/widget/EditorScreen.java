package net.lopymine.ip.editor.widget;

import net.lopymine.ip.yacl.screen.CursorTooltipPositioner;
import net.lopymine.mossylib.yacl.custom.MossyScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if <1.21.11 {
/*import net.minecraft.client.gui.components.events.GuiEventListener;
*///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
//? if <1.21.11 {
/*import org.jetbrains.annotations.Nullable;
*///?}

public abstract class EditorScreen extends Screen implements MossyScreen {

	protected EditorScreen(Component title) {
		super(title);
	}

	protected abstract void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks);

	//? if >=26.1 {
	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
	//?} else {
	/*@Override
	public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
	*///?}
		this.renderEditor(context, mouseX, mouseY, deltaTicks);
		CursorTooltipPositioner.renderPending(context);
	}

	protected void renderPanels(GuiGraphicsExtractor context) { }

	//? if >=26.1 {
	@Override
	public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractBackground(context, mouseX, mouseY, deltaTicks);
		this.renderPanels(context);
	}
	//?} elif >=1.21.1 {
	/*@Override
	public void renderBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.renderBackground(context, mouseX, mouseY, deltaTicks);
		this.renderPanels(context);
	}
	*///?} else {
	/*@Override
	public void renderBackground(GuiGraphicsExtractor context) {
		super.renderBackground(context);
		this.renderPanels(context);
	}
	*///?}

	//? if <1.21.11 {
	/*@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		if (this.getFocused() != focused) {
			super.setFocused(focused);
		}
	}
	*///?}

	protected void renderScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		//? if >=26.1 {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);
		//?} elif >=1.21.1 {
		/*super.render(context, mouseX, mouseY, deltaTicks);
		*///?} else {
		/*this.renderBackground(context);
		super.render(context, mouseX, mouseY, deltaTicks);
		*///?}
	}
}
