package net.lopymine.ip.editor.field;

import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.utils.*;
import net.lopymine.mossylib.extension.DrawContextExtension;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

@ExtensionMethod(DrawContextExtension.class)
public class NumberScrollWidget extends AbstractWidget {

	public static final int WIDTH = 6;
	public static final int HEIGHT = 16;

	private static final Identifier TEXTURE = InventoryParticles.id("textures/gui/spinner/spinner.png");

	private static final double STEP = 0.1D;
	private static final double LARGE_STEP = 0.5D;
	private static final long TOOLTIP_HIDE_TIME = 700L;

	private final FieldAccess access;
	private final EditBox box;

	private long lastScrollTime;

	public NumberScrollWidget(FieldAccess access, EditBox box) {
		super(0, 0, WIDTH, HEIGHT, Component.empty());
		this.access = access;
		this.box = box;
	}

	public boolean isScrolling() {
		return Util.getMillis() - this.lastScrollTime < TOOLTIP_HIDE_TIME;
	}

	//? if >=1.21.1 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
	*///?}
		if (!this.active || verticalAmount == 0.0D || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}

		this.lastScrollTime = Util.getMillis();
		this.step(InputUtils.hasShiftDown() ? LARGE_STEP : STEP, verticalAmount > 0.0D ? 1 : -1);
		return true;
	}

	private void step(double step, int direction) {
		Object value = FieldValues.stepNumber(this.access.type(), this.access.get(), step, direction);
		if (value == null) {
			return;
		}
		this.box.setValue(FieldValues.toText(this.access.type(), value));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		DrawUtils.drawTexture(context, TEXTURE, this.getX(), this.getY(), 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}
}
