package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.mossylib.MossyLib;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {

	@Shadow public abstract ItemStack getCarried();

	@Shadow @Final public NonNullList<Slot> slots;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;doClick(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V"), method = "clicked")
	private void spawnParticlesWhenPuttedInSlot(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
		this.stuff(slotIndex, actionType, player);
	}

	@Unique
	private void stuff(int slotIndex, ContainerInput actionType, Player player) {
		Level world = player.level();
		if (!world.isClientSide()) {
			return;
		}
		InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
		if (slotIndex >= 0 && slotIndex < this.slots.size() && config.isGuiActionsSpawnEnabled()) {
			boolean isTake = actionType == ContainerInput.PICKUP && this.getCarried().isEmpty();
			boolean isPut = actionType == ContainerInput.PICKUP && !this.getCarried().isEmpty();
			boolean isQuickMove = actionType == ContainerInput.QUICK_MOVE;
			if (!(config.isGuiActionTakeSpawnEnabled() && isTake)
					&& !(config.isGuiActionPutSpawnEnabled() && isPut)
					&& !(config.isGuiActionQuickMoveSpawnEnabled() && isQuickMove)
			) {
				return;
			}

			Screen currentScreen = ScreenUtils.current();
			if (!(currentScreen instanceof AbstractContainerScreen<?> handledScreen)) {
				return;
			}
			var slots = handledScreen.getMenu().slots;
			if (slotIndex >= slots.size()) {
				return;
			}
			Slot slot = slots.get(slotIndex);
			ItemStack stack = isTake || isPut ? this.getCarried() : slot.getItem();

			InventoryParticlesRenderer.getInstance().onGuiAction(slot, stack, handledScreen.leftPos, handledScreen.topPos);
		}
	}

}
