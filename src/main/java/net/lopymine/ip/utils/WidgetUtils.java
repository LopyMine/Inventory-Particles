package net.lopymine.ip.utils;

//? if <1.21.11 {
/*import net.lopymine.mossylib.extension.DrawContextExtension;
*///?}
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetUtils {

	//? if <1.21.11 {
	/*private static final float ABOVE_DEPTH = 300.0F;
	*///?}

	public static void render(AbstractWidget widget, GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		//? if >=26.1 {
		widget.extractRenderState(context, mouseX, mouseY, deltaTicks);
		//?} else {
		/*widget.render(context, mouseX, mouseY, deltaTicks);
		*///?}
	}

	public static void pushAbove(GuiGraphicsExtractor context) {
		//? if <1.21.11 {
		/*DrawContextExtension.push(context);
		DrawContextExtension.translate(context, 0.0F, 0.0F, ABOVE_DEPTH);
		*///?}
	}

	public static void popAbove(GuiGraphicsExtractor context) {
		//? if <1.21.11 {
		/*DrawContextExtension.pop(context);
		*///?}
	}
}
