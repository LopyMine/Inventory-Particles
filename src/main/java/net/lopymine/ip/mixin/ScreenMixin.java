package net.lopymine.ip.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.*;
import java.util.Map.Entry;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.element.mod.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ip.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow public int width;

	@Shadow public int height;

	@Inject(
			at = @At(
					value = "INVOKE",
					//? if >=26.1 {
					target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
					//?} else {
					/*target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
					*///?}
					shift = Shift.AFTER
			),
			//? if >=26.1 {
			method = "extractRenderStateWithTooltipAndSubtitles"
			//?} elif >=1.21.9 {
			/*method = "renderWithTooltipAndSubtitles"
			*///?} else {
			/*method = "renderWithTooltip"
			*///?}
	)
	private void renderInventoryParticles(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}

		Screen screen = (Screen) (Object) this;
		if (screen instanceof AbstractContainerScreen<?> handledScreen) {
			InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
			cursor.setHoveredSlot(handledScreen.hoveredSlot);
		}

		//? if >=1.21.5 {
		float tickProgress = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		//?} elif >=1.21.4 {
		/*float tickProgress = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		*///?} elif >=1.21.1 {
		/*float tickProgress = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
		*///?} else {
		/*float tickProgress = Minecraft.getInstance().getFrameTime();
		 *///?}

		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();

		Map<Integer, List<Integer>> map = NativeImageUtils.LIST.get(cursor.getCurrentStack().getItem());
		Map<Integer, Integer> map2 = NativeImageUtils.MAP2.get(cursor.getCurrentStack().getItem());

		if (map != null) {
			int xOffset = 0;
			for (Entry<Integer, List<Integer>> entry : map.entrySet()) {
				Integer color = entry.getKey();

				Font font = Minecraft.getInstance().font;
				String s = String.valueOf(ArgbUtils2.getRed(color));
				context.text(font, s, 100 + (xOffset * 4) - (font.width(s) / 2), 10, -1);

				context.fill(10 + xOffset, 20, 14 + xOffset, 24, color);

				for (int i = 0; i < entry.getValue().size(); i++) {
					int y = 10 + 28 + (6 * i);
					Integer col = entry.getValue().get(i);

					Integer c = map2.get(color);
					if (c != null && c.equals(col)) {
						context.fillGradient(10 + xOffset - 1, y - 1, 14 + xOffset + 1, y + 4 + 1, ArgbUtils2.getArgb(255, 255, 0, 0), ArgbUtils2.getArgb(255, 0, 0, 255));

						String ss1 = String.valueOf("A " + ArgbUtils2.getAlpha(col));
						context.text(font, ss1, 100 + (xOffset * 6) - (font.width(ss1) / 2), 20 + 20, -1);

						String ss2 = String.valueOf("R " + ArgbUtils2.getRed(col));
						context.text(font, ss2, 100 + (xOffset * 6) - (font.width(ss2) / 2), 30+ 20, -1);

						String ss3 = String.valueOf("G " + ArgbUtils2.getGreen(col));
						context.text(font, ss3, 100 + (xOffset * 6) - (font.width(ss3) / 2), 40+ 20, -1);

						String ss4 = String.valueOf("B " + ArgbUtils2.getBlue(col));
						context.text(font, ss4, 100 + (xOffset * 6) - (font.width(ss4) / 2), 50+ 20, -1);
					}

					context.fill(10 + xOffset, y, 14 + xOffset, y + 4, col);
				}

				xOffset+=6;
			}
		}


		InventoryParticlesRenderer.getInstance().render(context, tickProgress);
		if (!config.isDebugModeEnabled()) {
			return;
		}
		//? if >=1.21.6 {
		context.nextStratum();
		//?}
		InventoryParticlesClient.DEBUG_CURSOR_INFO_RENDERER.render(context);
		InventoryParticlesClient.DEBUG_PARTICLE_INFO_RENDERER.render(context);
		//? if >=1.21.6 {
		context.nextStratum();
		//?}
	}

	//? if <=1.21.4 {
	/*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;II)V"), method = "renderWithTooltip")
	private void fixTooltip(GuiGraphicsExtractor instance, Font textRenderer, List<FormattedCharSequence> text, ClientTooltipPositioner positioner, int x, int y, Operation<Void> original) {
		boolean bl = InventoryParticlesConfig.getInstance().getMainConfig().isModEnabled();
		if (bl) {
			RenderSystem.disableDepthTest();
		}
		original.call(instance, textRenderer, text, positioner, x, y);
		if (bl) {
			RenderSystem.enableDepthTest();
		}
	}
	*///?}
}