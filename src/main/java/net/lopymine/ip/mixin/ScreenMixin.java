package net.lopymine.ip.mixin;

import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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

		InventoryParticlesRenderer.getInstance().render(context, delta);
		if (!config.isDebugModeEnabled()) {
			return;
		}
		context.createNewRootLayer();
		InventoryParticlesClient.DEBUG_CURSOR_INFO_RENDERER.render(context);
		InventoryParticlesClient.DEBUG_PARTICLE_INFO_RENDERER.render(context);
		context.createNewRootLayer();
	}
}