package net.lopymine.ip.utils;

import net.minecraft.client.Minecraft;

public class TickUtils {

	public static float getRealtimeDeltaTicks() {
		//? if >=1.21.11 {
		return Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
		//?} elif >=1.21.1 {
		/*return Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
		*///?} else {
		/*return Minecraft.getInstance().getDeltaFrameTime();
		*///?}
	}
}
