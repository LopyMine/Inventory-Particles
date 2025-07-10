package net.lopymine.mossy.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.lopymine.mossy.client.MossyClient;

@Mixin(TitleScreen.class)
public class ExampleMixin {

	@Inject(at = @At("HEAD"), method = "init")
	private void init(CallbackInfo info) {
		MossyClient.LOGGER.info("Hello from Mossy Mixins!");
	}
}