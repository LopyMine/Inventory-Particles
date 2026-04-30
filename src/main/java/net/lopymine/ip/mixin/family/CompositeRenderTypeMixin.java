package net.lopymine.ip.mixin.family;

//? if 1.21.5 {


/*import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.lopymine.ip.family.generation.ItemRendering;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompositeRenderType.class)
public class CompositeRenderTypeMixin {

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderStateShard$OutputStateShard;getRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"), method = "draw")
	private RenderTarget swapTarget(OutputStateShard instance, Operation<RenderTarget> original) {
		if (ItemRendering.SWAP_TARGET) {
			return ItemRendering.TARGET;
		}
		return original.call(instance);
	}

}

*///?}
