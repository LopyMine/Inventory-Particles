package net.lopymine.ip.mixin;

import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.config.sub.InventoryParticleConfig;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.*;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

	@Shadow public abstract ItemStack getCursorStack();

	@Shadow @Final public DefaultedList<Slot> slots;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onSlotClick")
	private void spawnParticlesWhenPuttedInSlot(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		InventoryParticleConfig config = InventoryParticlesConfig.getInstance().getParticleConfig();
		if (slotIndex >= 0 && slotIndex < this.slots.size() && config.isGuiActionSpawnEnabled()) {
			boolean isTake = actionType == SlotActionType.PICKUP && this.getCursorStack().isEmpty();
			boolean isPut = actionType == SlotActionType.PICKUP && !this.getCursorStack().isEmpty();
			if (!(config.isGuiActionTakeSpawnEnabled() && isTake) && !(config.isGuiActionPutSpawnEnabled() && isPut)) {
				return;
			}

			Screen currentScreen = MinecraftClient.getInstance().currentScreen;
			if (!(currentScreen instanceof HandledScreen<?> handledScreen)) {
				return;
			}
			Slot slot = this.slots.get(slotIndex);
			boolean bl = currentScreen instanceof CreativeInventoryScreen;
			int offset = bl ? 32 : 0;
			InventoryParticlesRenderer.getInstance().onPutInSlot(slot, this.getCursorStack(), handledScreen.x, handledScreen.y - offset);
		}
	}

}
