package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.fabricmc.loader.api.FabricLoader;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMain;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> extends Screen {

	@Shadow @Final protected T handler;

	@Shadow protected int x;

	@Shadow protected int y;

	protected HandledScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("TAIL"), method = "init")
	private void addDebugButton(CallbackInfo ci) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			InventoryParticlesMain config = InventoryParticlesConfig.getInstance().getMainConfig();
			if (!config.isDebugModeEnabled() || !config.isModEnabled()) {
				return;
			}
			ButtonWidget stopTickingButton = this.addDrawableChild(ButtonWidget.builder(Text.of("Stop Ticking"), (button) -> {
				InventoryParticlesRenderer renderer = InventoryParticlesRenderer.getInstance();
				renderer.setStopTicking(!renderer.isStopTicking());
			}).position(5, this.height - 25).build());
			TextFieldWidget ticksPerTickField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, stopTickingButton.getX(), stopTickingButton.getY() - 25, stopTickingButton.getWidth(), 20, Text.literal("Ticks Per Tick"));
			ticksPerTickField.setChangedListener((s) -> {
				try {
					InventoryParticlesRenderer.getInstance().setTicksPerTick(Integer.parseInt(s));
				} catch (Exception ignored) {
				}
			});
			ticksPerTickField.setPlaceholder(Text.of("1"));
			this.addDrawableChild(ticksPerTickField);
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickInventoryParticles(CallbackInfo ci) {
		InventoryParticlesMain config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}
		InventoryParticlesRenderer.getInstance().tick(this.handler, this.x, this.y);
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"), method = "mouseClicked")
	private boolean addParticleFocusing(HandledScreen<?> instance, double x, double y, int button, Operation<Boolean> original) {
		InventoryParticlesMain config = InventoryParticlesConfig.getInstance().getMainConfig();
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
