package net.lopymine.ip.family;

import java.util.*;
import net.lopymine.ip.client.command.tags.TagsCommand;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

public class FamilyParticlesManager {

	public static List<FamilyParticleConfig> getFamilyConfigsForItem(Item item) {
		if (!InventoryParticlesConfig.getInstance().getFamilyGenerationConfig().canGenerateFor(item)) {
			return new ArrayList<>();
		}

		List<FamilyParticleConfig> data = getFamiliesByItemData(item);
		data.add(FamilyParticlesConfigManager.getInstance().getFallbackConfig());
		return data;
	}

	@NotNull
	private static List<FamilyParticleConfig> getFamiliesByItemData(Item item) {
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		ItemMatchData itemData = ItemMatchData.of(id);

		ArrayList<FamilyParticleConfig> configs = new ArrayList<>();
		for (FamilyParticleConfig config : FamilyParticlesConfigManager.getInstance().getRegisteredConfigs()) {
			if (matchKeywords(itemData, config.getKeywords().getBlacklist())) {
				continue;
			}
			if (matchTags(itemData, config.getTags().getBlacklist())) {
				continue;
			}
			if (matchNamespaces(itemData, config.getNamespaces().getBlacklist())) {
				continue;
			}

			if (matchKeywords(itemData, config.getKeywords().getWhitelist())) {
				configs.add(config);
			}
			if (matchTags(itemData, config.getTags().getWhitelist())) {
				configs.add(config);
			}
			if (matchNamespaces(itemData, config.getNamespaces().getWhitelist())) {
				configs.add(config);
			}
		}

		return configs;
	}

	private static boolean matchKeywords(ItemMatchData itemData, ArrayList<String> list) {
		for (String key : itemData.keys()) {
			if (list.contains(key)) {
				return true;
			}
		}

		for (String keyword : list) {
			if (keyword.startsWith("@") && itemData.path().contains(keyword.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private static boolean matchTags(ItemMatchData itemData, ArrayList<String> list) {
		List<String> tags = itemData.tags();
		if (tags == null) {
			return false;
		}

		for (String tag : tags) {
			if (list.contains(tag)) {
				return true;
			}
		}

		for (String tag : tags) {
			if (tag.startsWith("@") && itemData.path().contains(tag.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private static boolean matchNamespaces(ItemMatchData itemData, ArrayList<String> list) {
		String namespace = itemData.namespace();

		if (list.contains(namespace)) {
			return true;
		}

		for (String n : list) {
			if (n.startsWith("@") && namespace.contains(n.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private record ItemMatchData(String path, String namespace, String[] keys, @Nullable List<String> tags) {

		static ItemMatchData of(Identifier itemId) {
			String path = itemId.getPath();
			return new ItemMatchData(path, itemId.getNamespace(), path.split("_"), TagsCommand.getTags(itemId));
		}
	}

}
