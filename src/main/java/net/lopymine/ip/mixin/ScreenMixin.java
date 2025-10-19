package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow public int width;

	@Shadow public int height;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.AFTER), method = "renderWithTooltip")
	private void renderInventoryParticles(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}

		Screen screen = (Screen) (Object) this;
		if (screen instanceof HandledScreen<?> handledScreen) {
			InventoryParticlesRenderer.getInstance().updateCursor(mouseY, mouseX, handledScreen.getScreenHandler().getCursorStack(), handledScreen.focusedSlot);
		}


		//? if >=1.21.5 {
		/*float tickProgress = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
		*///?} elif >=1.21 && <=1.21.4 {
		/*float tickProgress = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
		*///?} else {
		float tickProgress = MinecraftClient.getInstance().getTickDelta();
		//?}
		InventoryParticlesRenderer.getInstance().render(context, tickProgress);
		if (!config.isDebugModeEnabled()) {
			return;
		}
		//? if >=1.21.6 {
		/*context.createNewRootLayer();
		*///?}
		InventoryParticlesClient.DEBUG_CURSOR_INFO_RENDERER.render(context);
		InventoryParticlesClient.DEBUG_PARTICLE_INFO_RENDERER.render(context);
		//? if >=1.21.6 {
		/*context.createNewRootLayer();
		*///?}
	}

	//? if <=1.21.4 {
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Lnet/minecraft/client/gui/tooltip/TooltipPositioner;II)V"), method = "renderWithTooltip")
	private void fixTooltip(DrawContext instance, TextRenderer textRenderer, List<OrderedText> text, TooltipPositioner positioner, int x, int y, Operation<Void> original) {
		boolean bl = InventoryParticlesConfig.getInstance().getMainConfig().isModEnabled();
		if (bl) {
			RenderSystem.disableDepthTest();
		}
		original.call(instance, textRenderer, text, positioner, x, y);
		if (bl) {
			RenderSystem.enableDepthTest();
		}
	}
	//?}
}