package net.lopymine.ip.family;

import java.util.*;
import net.lopymine.ip.client.command.tags.TagsCommand;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

public class FamilyParticlesManager {

	public static List<FamilyParticleConfig> getFamily(Item item) {
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

		ArrayList<FamilyParticleConfig> configs = new ArrayList<>();
		for (FamilyParticleConfig config : FamilyParticlesConfigManager.getInstance().getRegisteredConfigs()) {
			if (matchKeywords(id, config.getKeywords().getBlacklist())) {
				continue;
			}
			if (matchTags(id, config.getTags().getBlacklist())) {
				continue;
			}
			if (matchNamespaces(id, config.getNamespaces().getBlacklist())) {
				continue;
			}

			if (matchKeywords(id, config.getKeywords().getWhitelist())) {
				configs.add(config);
			}
			if (matchTags(id, config.getTags().getWhitelist())) {
				configs.add(config);
			}
			if (matchNamespaces(id, config.getNamespaces().getWhitelist())) {
				configs.add(config);
			}
		}

		return configs;
	}

	private static boolean matchKeywords(Identifier itemId, ArrayList<String> list) {
		String path = itemId.getPath();
		String[] keys = path.split("_");

		for (String key : keys) {
			if (list.contains(key)) {
				return true;
			}
		}

		for (String keyword : list) {
			if (keyword.startsWith("@") && path.contains(keyword.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private static boolean matchTags(Identifier itemId, ArrayList<String> list) {
		String path = itemId.getPath();
		List<String> tags = TagsCommand.getTags(itemId);
		if (tags == null) {
			return false;
		}

		for (String tag : tags) {
			if (list.contains(tag)) {
				return true;
			}
		}

		for (String tag : tags) {
			if (tag.startsWith("@") && path.contains(tag.substring(1))) {
				return true;
			}
		}

		return false;
	}

	private static boolean matchNamespaces(Identifier itemId, ArrayList<String> list) {
		String namespace = itemId.getNamespace();

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

}
