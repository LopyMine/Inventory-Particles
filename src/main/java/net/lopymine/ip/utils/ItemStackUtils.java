package net.lopymine.ip.utils;

//? if >=26.1 {
import net.minecraft.core.component.*;
//?}
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class ItemStackUtils {

	public static ItemStack create(@Nullable Item item) {
		if (item == null) {
			return ItemStack.EMPTY;
		}
		//? if >=26.1 {
		return item.getDefaultInstance();
		//return new ItemStack(Holder.direct(item, DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build()
		//));
		//?} else {
		/*return item.getDefaultInstance();
		*///?}
	}
}
