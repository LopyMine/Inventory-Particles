package net.lopymine.ip;

import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.slf4j.*;

import net.fabricmc.api.ModInitializer;

public class InventoryParticles implements ModInitializer {

	public static final String MOD_NAME = /*$ mod_name*/ "Inventory Particles";
	public static final String MOD_ID = /*$ mod_id*/ "inventory-particles";
	public static final String YACL_DEPEND_VERSION = /*$ yacl*/ "3.6.6+1.20.1-fabric";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static MutableText text(String path, Object... args) {
		return Text.translatable(String.format("%s.%s", MOD_ID, path), args);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("{} Initialized", MOD_NAME);
	}
}