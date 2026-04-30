package net.lopymine.ip.mixin.family;

//? if <=1.21.4 {

/*import com.mojang.blaze3d.pipeline.RenderTarget;
import net.lopymine.ip.family.generation.ItemRendering;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(at = @At(value = "RETURN"), method = "getMainRenderTarget", cancellable = true)
	private void swapTarget(CallbackInfoReturnable<RenderTarget> cir) {
		if (ItemRendering.SWAP_TARGET) {
			cir.setReturnValue(ItemRendering.TARGET);
		}
	}

}
*///?}
