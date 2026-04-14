//~ client_fabric_commands

package net.lopymine.ip.client.command.tags;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.*;
import com.mojang.datafixers.util.Pair;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.mossylib.loader.MossyLoader;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import static net.lopymine.mossylib.utils.CommandUtils.argument;
import static net.lopymine.mossylib.utils.CommandUtils.literal;

//? if >=1.21.11 {

import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

//?} else {
/*import net.minecraft.Util;
*///?}

public class TagsCommand {

	public static final Path FOLDER = MossyLoader.getConfigDir().resolve(InventoryParticles.MOD_ID.replace("_", "-"));

	public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
		return literal("tags")
				.then(literal("items-for-tag")
						.then(argument("tag", IdentifierArgument.id())
								.suggests((context, builder) ->
										SharedSuggestionProvider.suggest(getAvailableTags(), builder))
								.executes(TagsCommand::items)))
				.then(literal("tags-in-item")
						.then(argument("item", IdentifierArgument.id())
								.suggests((context, builder) ->
										SharedSuggestionProvider.suggest(getAvailableItems(), builder))
								.executes(TagsCommand::tags)))
				.then(literal("list").executes(TagsCommand::list));
	}

	//? if <=1.21.1 {
	/*@SuppressWarnings("deprecation")
	*///?}
	private static int tags(CommandContext<FabricClientCommandSource> context) {
		Identifier item = context.getArgument("item", Identifier.class);

		if (!getAvailableItems().contains(item.toString())) {
			return 0;
		}
		List<String> list = getTags(item);
		if (list == null) return 0;

		writeAndOpen("tags-in-item.txt", list);
		return Command.SINGLE_SUCCESS;
	}

	public static @Nullable List<String> getTags(Identifier item) {
		//? if >=1.21.4 {
		Optional<Reference<Item>> optional = BuiltInRegistries.ITEM.get(item);
		if (optional.isEmpty()) {
			return null;
		}

		return optional.get().tags().map(TagKey::location).map(Identifier::toString).toList();
		//?} else {
		/*Item get = BuiltInRegistries.ITEM.get(item);
		return get.builtInRegistryHolder().tags().map(TagKey::location).map(Identifier::toString).toList();
		*///?}
	}

	private static int items(CommandContext<FabricClientCommandSource> context) {
		Identifier tag = context.getArgument("tag", Identifier.class);

		if (!getAvailableTags().contains(tag.toString())) {
			return 0;
		}

		//? if >=1.21.4 {
		Optional<Named<Item>> optional = BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, tag));
		//?} else {
		/*Optional<Named<Item>> optional = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag));
		*///?}
		if (optional.isEmpty()) {
			return 0;
		}

		List<String> list = optional.get().stream().map(Holder::value).map(BuiltInRegistries.ITEM::getKey).map(Identifier::toString).toList();
		writeAndOpen("items-with-tag.txt", list);
		return Command.SINGLE_SUCCESS;
	}

	private static int list(CommandContext<FabricClientCommandSource> context) {
		writeAndOpen("available-tags.txt", getAvailableTags());
		return Command.SINGLE_SUCCESS;
	}

	private static void writeAndOpen(String file, List<String> list) {
		String text = String.join("\n", list);
		try {
			if (!Files.exists(FOLDER)) {
				Files.createDirectory(FOLDER);
			}

			Path path = FOLDER.resolve(file);
			if (!Files.exists(path)) {
				Files.createFile(path);
			}
			Files.writeString(path, text);
			Util.getPlatform().openUri(path.toUri());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static @NotNull List<String> getAvailableTags() {
		//? if >=1.21.4 {
		return BuiltInRegistries.ITEM.listTagIds().map(TagKey::location).map(Identifier::toString).toList();
		//?} else {
		/*return BuiltInRegistries.ITEM.getTags().map(Pair::getFirst).map(TagKey::location).map(Identifier::toString).toList();
		*///?}
	}

	private static @NotNull List<String> getAvailableItems() {
		//? if >=1.21.4 {
		return BuiltInRegistries.ITEM.listElementIds().map(ResourceKey::identifier).map(Identifier::toString).toList();
		//?} else {
		/*return BuiltInRegistries.ITEM.entrySet().stream().map(Entry::getKey).map(ResourceKey::identifier).map(Identifier::toString).toList();
		*///?}
	}

}
