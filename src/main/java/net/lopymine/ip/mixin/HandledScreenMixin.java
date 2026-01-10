package net.lopymine.ip.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import java.util.function.Supplier;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticlesMainConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen {

	@Shadow @Final protected T menu;

	@Shadow protected int leftPos;

	@Shadow protected int topPos;

	protected HandledScreenMixin(Component title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tickInventoryParticles(CallbackInfo ci) {
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isModEnabled()) {
			return;
		}

		InventoryParticlesRenderer.getInstance().getCursor().setCurrentStack(this.menu.getCarried());

		InventoryParticlesRenderer.getInstance().tick(this.menu, this.leftPos, this.topPos);
	}

	//? if >=1.21.9 {
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"), method = "mouseClicked")
	private boolean addParticleFocusing(AbstractContainerScreen<?> instance, MouseButtonEvent click, boolean b, Operation<Boolean> original) {
		boolean bl = original.call(instance, click, b);
		int button = click.button();
		//?} else {
	/*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"), method = "mouseClicked")
	private boolean addParticleFocusing(AbstractContainerScreen<?> instance, double x, double y, int button, Operation<Boolean> original) {
			boolean bl = original.call(instance, x, y, button);
	*///?}
		InventoryParticlesMainConfig config = InventoryParticlesConfig.getInstance().getMainConfig();
		if (!config.isDebugModeEnabled() || !config.isModEnabled()) {
			return bl;
		}
		if (!bl) {
			InventoryParticlesRenderer.getInstance().mouseClicked(button);
		}
		return bl;
	}

}
