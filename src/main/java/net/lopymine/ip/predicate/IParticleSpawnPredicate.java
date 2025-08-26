package net.lopymine.ip.predicate;

import net.minecraft.item.ItemStack;

public interface IParticleSpawnPredicate {

	boolean test(ItemStack stack);

}
