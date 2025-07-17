package net.lopymine.ip.utils;

import net.lopymine.ip.InventoryParticles;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import net.lopymine.ip.yacl.utils.SimpleContent;

import java.util.function.Function;

public class ModMenuUtils {

	public static String getOptionKey(String optionId) {
		return String.format("modmenu.option.%s", optionId);
	}

	public static String getCategoryKey(String categoryId) {
		return String.format("modmenu.category.%s", categoryId);
	}

	public static String getGroupKey(String groupId) {
		return String.format("modmenu.group.%s", groupId);
	}

	public static MutableText getName(String key) {
		return InventoryParticles.text(key + ".name");
	}

	public static MutableText getDescription(String key) {
		return InventoryParticles.text(key + ".description");
	}

	public static Identifier getContentId(SimpleContent content, String contentId) {
		return InventoryParticles.id(String.format("textures/config/%s.%s", contentId, content.getFileExtension()));
	}

	public static MutableText getModTitle() {
		return InventoryParticles.text("modmenu.title");
	}

	public static Function<Boolean, Text> getEnabledOrDisabledFormatter() {
		return state -> InventoryParticles.text("modmenu.formatter.enabled_or_disabled." + state);
	}

	public static MutableText getNoConfigScreenMessage() {
		return InventoryParticles.text("modmenu.no_config_library_screen.message");
	}

	public static MutableText getOldConfigScreenMessage(String version) {
		return InventoryParticles.text("modmenu.old_config_library_screen.message", version, InventoryParticles.YACL_DEPEND_VERSION);
	}
}
