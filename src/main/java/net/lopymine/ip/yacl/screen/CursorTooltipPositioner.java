package net.lopymine.ip.yacl.screen;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class CursorTooltipPositioner implements ClientTooltipPositioner {

	public static final CursorTooltipPositioner INSTANCE = new CursorTooltipPositioner();

	private static final int OFFSET_X = 10;
	private static final int OFFSET_Y = 10;
	private static final int MAX_WIDTH = 220;

	//? if <1.21.11 {
	/*private static List<Component> pendingLines = List.of();
	private static int pendingX;
	private static int pendingY;
	*///?}

	public static void render(GuiGraphicsExtractor context, List<Component> lines, int mouseX, int mouseY) {
		if (lines.isEmpty()) {
			return;
		}

		//? if >=1.21.11 {
		context.setTooltipForNextFrame(Minecraft.getInstance().font, split(lines), INSTANCE, mouseX, mouseY, false);
		//?} else {
		/*pendingLines = lines;
		pendingX     = mouseX;
		pendingY     = mouseY;
		*///?}
	}

	public static void renderPending(GuiGraphicsExtractor context) {
		//? if <1.21.11 {
		/*if (pendingLines.isEmpty()) {
			return;
		}

		context.renderTooltip(Minecraft.getInstance().font, split(pendingLines), INSTANCE, pendingX, pendingY);
		pendingLines = List.of();
		*///?}
	}

	private static List<FormattedCharSequence> split(List<Component> lines) {
		Font font = Minecraft.getInstance().font;

		List<FormattedCharSequence> sequences = new ArrayList<>();
		for (Component line : lines) {
			sequences.addAll(font.split(line, MAX_WIDTH));
		}
		return sequences;
	}

	@Override
	public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
		return new Vector2i(
				flip(mouseX, OFFSET_X, tooltipWidth, screenWidth),
				flip(mouseY, OFFSET_Y, tooltipHeight, screenHeight)
		);
	}

	private static int flip(int mouse, int offset, int size, int screenSize) {
		int position = mouse + offset;
		if (position + size <= screenSize) {
			return position;
		}

		int flipped = mouse - offset - size;
		return flipped < 0 ? Math.max(0, screenSize - size) : flipped;
	}
}
