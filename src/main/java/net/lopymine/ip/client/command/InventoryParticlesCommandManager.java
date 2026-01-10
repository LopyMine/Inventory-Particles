package net.lopymine.ip.client.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.command.tags.TagsCommand;
import static net.lopymine.mossylib.utils.CommandUtils.literal;

public class InventoryParticlesCommandManager {

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal(InventoryParticles.MOD_ID.replace("_", "-"))
				.then(TagsCommand.get()));
	}

}
