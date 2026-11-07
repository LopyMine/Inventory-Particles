package net.lopymine.ip.editor.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.lopymine.mossylib.yacl.custom.MossyScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

public class MossyConfirmScreen extends ConfirmScreen implements MossyScreen {

	public MossyConfirmScreen(BooleanConsumer callback, Component title, Component message, Component yesButton, Component noButton) {
		super(callback, title, message, yesButton, noButton);
	}
}
