package net.lopymine.ip.utils;

//? if >=1.21.11 {
import net.minecraft.client.Minecraft;
//?} else {
/*import net.minecraft.client.gui.screens.Screen;
*///?}

public class InputUtils {

	public static boolean hasControlDown() {
		//? if >=1.21.11 {
		return Minecraft.getInstance().hasControlDown();
		//?} else {
		/*return Screen.hasControlDown();
		*///?}
	}

	public static boolean hasShiftDown() {
		//? if >=1.21.11 {
		return Minecraft.getInstance().hasShiftDown();
		//?} else {
		/*return Screen.hasShiftDown();
		*///?}
	}
}
