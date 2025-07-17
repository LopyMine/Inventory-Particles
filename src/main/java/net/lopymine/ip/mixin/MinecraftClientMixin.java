package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"), method = "setScreen")
	private void clearRendererWhenRemovedScreen(Screen screen, CallbackInfo ci) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().clear();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V", shift = Shift.AFTER), method = "setScreen")
	private void initRendererWhenInitScreen(Screen screen, CallbackInfo ci) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().init();
	}

}
