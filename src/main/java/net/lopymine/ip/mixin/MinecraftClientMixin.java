package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow @Nullable public Screen currentScreen;

	@Shadow @Final private Window window;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"), method = "setScreen")
	private void clearRendererWhenRemovedScreen(Screen screen, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().clear();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V", shift = Shift.AFTER), method = "setScreen")
	private void initRendererWhenInitScreen(Screen screen, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().init();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;resize(Lnet/minecraft/client/MinecraftClient;II)V"), method = "onResolutionChanged")
	private void updateParticlesPositions(CallbackInfo ci) {
		Screen screen = this.currentScreen;
		if (screen == null) {
			return;
		}
		int oldWidth = screen.width;
		int oldHeight = screen.height;
		int width = this.window.getScaledWidth();
		int height = this.window.getScaledHeight();
		float x = (float) width / oldWidth;
		float y = (float) height / oldHeight;
		InventoryParticlesRenderer.getInstance().updateParticlesPositions(x, y);
	}

}
