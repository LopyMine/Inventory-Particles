package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> extends Screen {

	@Shadow @Final protected T handler;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("TAIL"), method = "init")
	private void addDebugButton(CallbackInfo ci) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isDebugModeEnabled() || !config.isModEnabled()) {
			return;
		}
		this.addDrawableChild(ButtonWidget.builder(Text.of("Stop Ticking"), (button) -> {
			InventoryParticlesRenderer renderer = InventoryParticlesRenderer.getInstance();
			renderer.setStopTicking(!renderer.isStopTicking());
		}).position(5, this.height - 25).build());
	}

	@Inject(at = @At("TAIL"), method = "render")
	private void updateCursor(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().updateCursor(mouseY, mouseX, this.handler.getCursorStack());
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickInventoryParticles(CallbackInfo ci) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().tick();
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"), method = "mouseClicked")
	private boolean wrapOperation(HandledScreen<?> instance, double x, double y, int button, Operation<Boolean> original) {
		InventoryParticlesConfig config = InventoryParticlesConfig.getInstance();
		if (!config.isDebugModeEnabled() || !config.isModEnabled()) {
			return original.call(instance, x, y, button);
		}
		Boolean noPressedWidgets = original.call(instance, x, y, button);
		if (!noPressedWidgets) {
			InventoryParticlesRenderer.getInstance().mouseClicked(button);
		}
		return noPressedWidgets;
	}

}
