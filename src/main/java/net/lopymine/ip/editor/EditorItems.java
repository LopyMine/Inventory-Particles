package net.lopymine.ip.editor;

import java.util.List;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.mossylib.utils.DrawUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class EditorItems {

	private static final Identifier UNKNOWN_TEXTURE = InventoryParticles.id("textures/gui/misc/unknown_item.png");
	private static final int TEXTURE_SIZE = 16;

	public static boolean isUnknown(CachedItem item) {
		return item.getItem() == Items.AIR && !item.getId().equals(CachedItem.AIR_ID);
	}

	public static void renderUnknown(GuiGraphicsExtractor context, int x, int y) {
		DrawUtils.drawTexture(context, UNKNOWN_TEXTURE, x, y, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
	}

	public static List<Component> getUnknownTooltip(Identifier id) {
		return List.of(
				InventoryParticles.text("editor.items.unknown"),
				Component.literal(id.toString()).withStyle(ChatFormatting.GRAY)
		);
	}
}
