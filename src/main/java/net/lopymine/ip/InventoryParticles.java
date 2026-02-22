package net.lopymine.ip;

import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import org.slf4j.*;

public class InventoryParticles {

	public static final String MOD_NAME = /*$ mod_name*/ "Inventory Particles";
	public static final String MOD_ID = /*$ mod_id*/ "inventory_particles";
	public static final MossyLogger LOGGER = new MossyLogger(MOD_NAME);

	public static Identifier id(String path) {
		//? if >=1.21 {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
		//?} else {
		/*return Identifier.tryBuild(MOD_ID, path);
		 *///?}
	}

	public static Identifier parseId(String path) {
		//? if >=1.21 {
		return Identifier.parse(path);
		//?} else {
		/*return new Identifier(path);
		 *///?}
	}

	public static MutableComponent text(String path, Object... args) {
		return Component.translatable(String.format("%s.%s", MOD_ID, path), args);
	}

	public static void onInitialize() {
		LOGGER.info("{} Initialized", MOD_NAME);
	}
}