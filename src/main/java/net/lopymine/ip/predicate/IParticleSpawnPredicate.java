package net.lopymine.ip.predicate;

import net.minecraft.world.item.ItemStack;

public interface IParticleSpawnPredicate {

	boolean test(ItemStack stack);

}
