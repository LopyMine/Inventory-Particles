package net.lopymine.ip.mixin;

import net.lopymine.ip.family.utils.FamilySafeRenderExecutor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftRenderCallMixin {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V"), method = "runTick")
	private void myDearRenderCall(CallbackInfo ci) {
		FamilySafeRenderExecutor.run();
	}

}

