package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
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

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"), method = "clicked")
	private void spawnParticlesWhenPuttedInSlot(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
		Level world = player.level();
		if (!world.isClientSide()) {
			return;
		}
		InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
		if (slotIndex >= 0 && slotIndex < this.slots.size() && config.isGuiActionSpawnEnabled()) {
			boolean isTake = actionType == ClickType.PICKUP && this.getCarried().isEmpty();
			boolean isPut = actionType == ClickType.PICKUP && !this.getCarried().isEmpty();
			boolean isQuickMove = actionType == ClickType.QUICK_MOVE;
			if (!(config.isGuiActionTakeSpawnEnabled() && isTake)
					&& !(config.isGuiActionPutSpawnEnabled() && isPut)
					&& !(config.isGuiActionQuickMoveSpawnEnabled() && isQuickMove)
			) {
				return;
			}

			Screen currentScreen = Minecraft.getInstance().screen;
			if (!(currentScreen instanceof AbstractContainerScreen<?> handledScreen)) {
				return;
			}
			Slot slot = handledScreen.getMenu().slots.get(slotIndex);
			ItemStack stack = isTake || isPut ? this.getCarried() : slot.getItem();

			InventoryParticlesRenderer.getInstance().onGuiAction(slot, stack, handledScreen.leftPos, handledScreen.topPos);
		}
	}

}
