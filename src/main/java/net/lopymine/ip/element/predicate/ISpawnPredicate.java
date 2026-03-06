package net.lopymine.ip.element.predicate;

import net.minecraft.world.item.ItemStack;

public interface ISpawnPredicate {

	boolean test(ItemStack stack);

}
