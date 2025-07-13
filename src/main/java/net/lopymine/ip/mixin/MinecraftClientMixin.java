package net.lopymine.ip.mixin;

import net.lopymine.ip.renderer.InvParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"), method = "setScreen")
	private void clearInventoryParticles(Screen screen, CallbackInfo ci) {
		InvParticlesRenderer.getInstance().clear();
	}

}
