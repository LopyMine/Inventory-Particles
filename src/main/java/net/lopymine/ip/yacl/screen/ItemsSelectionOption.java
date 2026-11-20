package net.lopymine.ip.yacl.screen;

import dev.isxander.yacl3.api.*;
import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.editor.EditorItemsIndex;
import net.lopymine.ip.editor.screen.ItemSelectionScreen;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.lopymine.mossylib.yacl.api.*;
//? if >=1.21.11 {
import net.minecraft.client.Minecraft;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;

public class ItemsSelectionOption {

	public static ButtonOption create(String optionId, Option<String> itemsOption) {
		return SimpleOption.startButtonBuilder(optionId, (screen, button) -> open(screen, itemsOption))
				.withDescription(SimpleContent.NONE)
				.custom((builder) -> {
					if (isAvailable()) {
						return;
					}
					builder.available(false).text(InventoryParticles.text("editor.only_in_world"));
				})
				.build(InventoryParticles.MOD_ID);
	}

	public static boolean isAvailable() {
		//? if >=1.21.11 {
		return Minecraft.getInstance().level != null;
		//?} else {
		/*return true;
		*///?}
	}

	public static boolean isAvailable(Option<?> option) {
		return !(option instanceof ButtonOption) || isAvailable();
	}

	private static void open(Screen parent, Option<String> itemsOption) {
		EditorItemsIndex index = EditorItemsIndex.create();

		Map<String, Item> itemsById = new HashMap<>();
		for (Item item : index.getItems()) {
			itemsById.put(index.getId(item).toString(), item);
		}

		List<String> tokens = Arrays.stream(itemsOption.pendingValue().split(" "))
				.map(String::trim)
				.filter((token) -> !token.isEmpty())
				.toList();

		Set<Item> selectedItems = new LinkedHashSet<>();
		for (String token : tokens) {
			Item item = itemsById.get(normalize(token));
			if (item != null) {
				selectedItems.add(item);
			}
		}

		Set<Item> listedItems = new HashSet<>(selectedItems);

		ScreenUtils.set(new ItemSelectionScreen(parent, InventoryParticles.text("items_selection.title"), index, selectedItems, (items) -> {
			Set<String> result = new LinkedHashSet<>();
			for (String token : tokens) {
				Item item = itemsById.get(normalize(token));
				if (item == null || items.contains(item)) {
					result.add(token);
				}
			}
			for (Item item : items) {
				if (!listedItems.contains(item)) {
					result.add(index.getId(item).toString());
				}
			}

			itemsOption.requestSet(String.join(" ", result));
			ScreenUtils.set(parent);
		}));
	}

	private static String normalize(String token) {
		return token.contains(":") ? token : "minecraft:" + token;
	}

}
