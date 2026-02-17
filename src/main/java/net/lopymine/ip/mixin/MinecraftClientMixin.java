package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Window;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

	@Shadow @Nullable public Screen screen;

	@Shadow @Final private Window window;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"), method = "setScreen")
	private void clearRendererWhenRemovedScreen(Screen screen, CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().clear();
	}

	//? if >=1.21.11 {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(II)V", shift = Shift.AFTER), method = "setScreen")
	private void initRendererWhenInitScreen(Screen screen, CallbackInfo ci) {
		this.initRenderer();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;resize(II)V"), method = "resizeDisplay")
	private void updateParticlesPositions(CallbackInfo ci) {
		this.updateParticlesPositions();
	}
	//?} else {
	/*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V", shift = Shift.AFTER), method = "setScreen")
	private void initRendererWhenInitScreen(Screen screen, CallbackInfo ci) {
		this.initRenderer();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;resize(Lnet/minecraft/client/Minecraft;II)V"), method = "resizeDisplay")
	private void updateParticlesPositions(CallbackInfo ci) {
		this.updateParticlesPositions();
	}
	*///?}

	@Unique
	private void initRenderer() {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().init();
	}

	@Unique
	private void updateParticlesPositions() {
		Screen screen = this.screen;
		if (screen == null) {
			return;
		}
		int oldWidth = screen.width;
		int oldHeight = screen.height;
		int width = this.window.getGuiScaledWidth();
		int height = this.window.getGuiScaledHeight();
		double x = (double) width / (double) oldWidth;
		double y = (double) height / (double) oldHeight;
		InventoryParticlesRenderer.getInstance().updateElementsPositions(x, y);
	}

}
