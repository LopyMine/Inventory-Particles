package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
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
					target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
					shift = Shift.AFTER
			),
			//? if >=1.21.9 {
			method = "renderWithTooltipAndSubtitles"
			//?} else {
			/*method = "renderWithTooltip"
			*///?}
	)
	private void renderInventoryParticles(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}

		Screen screen = (Screen) (Object) this;
		if (screen instanceof AbstractContainerScreen<?> handledScreen) {
			InventoryParticlesRenderer.getInstance().updateCursor(mouseY, mouseX, handledScreen.getMenu().getCarried(), handledScreen.hoveredSlot);
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
	/*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;II)V"), method = "renderWithTooltip")
	private void fixTooltip(GuiGraphics instance, Font textRenderer, List<FormattedCharSequence> text, ClientTooltipPositioner positioner, int x, int y, Operation<Void> original) {
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