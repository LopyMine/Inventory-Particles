package net.lopymine.ip.mixin;

import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.renderer.InvParticlesRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.AFTER), method = "renderWithTooltip")
	private void renderInventoryParticles(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InvParticlesRenderer.getInstance().render(context, delta);
		InventoryParticlesClient.getCursorRenderer().render(context);
		InventoryParticlesClient.getParticleRenderer().render(context);
	}
}