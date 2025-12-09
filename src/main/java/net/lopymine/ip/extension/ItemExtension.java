package net.lopymine.ip.extension;

import net.minecraft.world.item.Item;

public class ItemExtension {

	public static String getStringName(Item item) {
		//? if >=1.21.4 {
		return item.getName().getString();
		//?} else {
		/*return item.getDescription().getString();
		*///?}
	}

}
