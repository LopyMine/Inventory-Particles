package net.lopymine.ip.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.lopymine.ip.client.renderer.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.*;
import net.minecraft.world.item.Item;
import org.slf4j.*;

import net.lopymine.ip.InventoryParticles;

public class InventoryParticlesClient {

	public static Logger LOGGER = LoggerFactory.getLogger(InventoryParticles.MOD_NAME + "/Client");

	public final static DebugParticleInfoRenderer DEBUG_PARTICLE_INFO_RENDERER = new DebugParticleInfoRenderer();
	public final static DebugCursorInfoRenderer DEBUG_CURSOR_INFO_RENDERER = new DebugCursorInfoRenderer();

	public static void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryParticles.MOD_NAME);

		ClientPlayConnectionEvents.JOIN.register((aa, bb, vv) -> {
			TagKey<Item> key = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyed/pink"));

			BuiltInRegistries.ITEM.listTagIds().forEach((tag) -> {
				System.out.println(tag.toString());
			});

			BuiltInRegistries.ITEM.get(key).ifPresent((a) -> {
				System.out.println(a);
				a.forEach((b) -> {
					System.out.println(b.value().getName().getString());
				});
			});
		});
	}
}
