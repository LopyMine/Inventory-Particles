package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.lopymine.ip.renderer.InvParticlesRenderer;
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
	private void inject(CallbackInfo ci) {
		this.addDrawableChild(ButtonWidget.builder(Text.of("Stop Ticking"), (button) -> {
			InvParticlesRenderer.getInstance().setStopTicking(!InvParticlesRenderer.getInstance().isStopTicking());
		}).position(5, this.height - 25).build());
	}

	@Inject(at = @At("TAIL"), method = "render")
	private void updateCursor(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		InvParticlesRenderer.getInstance().updateCursor(mouseY, mouseX, this.handler.getCursorStack().getItem());
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickInventoryParticles(CallbackInfo ci) {
		InvParticlesRenderer.getInstance().tick();
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"), method = "mouseClicked")
	private boolean wrapOperation(HandledScreen<?> instance, double x, double y, int button, Operation<Boolean> original) {
		Boolean bl = original.call(instance, x, y, button);
		if (!bl && button == 1) {
			InvParticlesRenderer.getInstance().lockHoveredParticle();
		}
		return bl;
	}

}
