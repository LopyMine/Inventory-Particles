package net.lopymine.ip.spawner.context;

import lombok.*;
import net.lopymine.ip.element.InventoryCursor;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

@Getter
@Setter
public class ParticleSpawnContext {

	private static final ParticleSpawnContext CURSOR_CONTEXT = new ParticleSpawnContext();
	private static final ParticleSpawnContext FOCUSED_SLOT_CONTEXT = new ParticleSpawnContext();
	private static final ParticleSpawnContext SLOT_CONTEXT = new ParticleSpawnContext();

	private ItemStack stack;
	private int x, y;
	private float impulseX, impulseY;

	public static ParticleSpawnContext cursor(InventoryCursor cursor) {
		CURSOR_CONTEXT.setStack(cursor.getCurrentStack());
		CURSOR_CONTEXT.setX(cursor.getX());
		CURSOR_CONTEXT.setY(cursor.getY());
		CURSOR_CONTEXT.setImpulseX(cursor.getSpeedX());
		CURSOR_CONTEXT.setImpulseY(cursor.getSpeedY());
		return CURSOR_CONTEXT;
	}

	public static ParticleSpawnContext slot(Slot slot, int inventoryX, int inventoryY) {
		SLOT_CONTEXT.setStack(slot.getStack());
		SLOT_CONTEXT.setX(inventoryX + slot.x + 8);
		SLOT_CONTEXT.setY(inventoryY + slot.y + 8);
		SLOT_CONTEXT.setImpulseX(0F);
		SLOT_CONTEXT.setImpulseY(0F);
		return SLOT_CONTEXT;
	}

	public static ParticleSpawnContext focusedSlot(Slot slot, int inventoryX, int inventoryY) {
		FOCUSED_SLOT_CONTEXT.setStack(slot.getStack());
		FOCUSED_SLOT_CONTEXT.setX(inventoryX + slot.x + 8);
		FOCUSED_SLOT_CONTEXT.setY(inventoryY + slot.y + 8);
		FOCUSED_SLOT_CONTEXT.setImpulseX(0F);
		FOCUSED_SLOT_CONTEXT.setImpulseY(0F);
		return FOCUSED_SLOT_CONTEXT;
	}

	public static boolean isSlot(ParticleSpawnContext context) {
		return context == SLOT_CONTEXT;
	}

	public static boolean isCursor(ParticleSpawnContext context) {
		return context == CURSOR_CONTEXT;
	}
}
