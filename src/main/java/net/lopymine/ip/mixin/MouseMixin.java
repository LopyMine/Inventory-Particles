package net.lopymine.ip.mixin;

import com.mojang.blaze3d.platform.Window;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

	@Shadow private double xpos;

	@Shadow private double ypos;

	@Inject(at = @At("TAIL"), method = "onMove")
	private void updateMousePosition(CallbackInfo ci) {
		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		Window window = Minecraft.getInstance().getWindow();
		cursor.setMouseX((int) (this.xpos * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth()));
		cursor.setMouseY((int) (this.ypos * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight()));
	}

	@Inject(
			at = @At("TAIL"),
			//? if >=1.21.9 {
			method = "onButton"
			//?} else {
			/*method = "onPress"
			*///?}
	)
	private void inject(CallbackInfo ci) {
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> handledScreen) {
			InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
			cursor.setCurrentStack(handledScreen.getMenu().getCarried());
		}
	}

}
