package net.lopymine.ip.client;

import net.lopymine.ip.client.renderer.*;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.mossylib.loader.MossyLoader;
import net.lopymine.mossylib.logger.MossyLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;

import net.lopymine.ip.InventoryParticles;
import net.minecraft.sounds.SoundEvents;

public class InventoryParticlesClient {

	public static MossyLogger LOGGER = InventoryParticles.LOGGER.extend("Client");

	public final static DebugParticleInfoRenderer DEBUG_PARTICLE_INFO_RENDERER = new DebugParticleInfoRenderer();
	public final static DebugCursorInfoRenderer DEBUG_CURSOR_INFO_RENDERER = new DebugCursorInfoRenderer();

	public static void onInitializeClient() {
		LOGGER.info("{} Client Initialized", InventoryParticles.MOD_NAME);

	}

	public static void sendNoticeMessage(int size) {
		if (InventoryParticlesConfig.getInstance().isTest210() || Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
			return;
		}

		String name = Minecraft.getInstance().getUser().getName();

		MutableComponent message = InventoryParticles.text("notice_message_2_1_0", name, size);

		ChatComponent chat = Minecraft.getInstance().gui.getChat();
		//? if >=26.1 {
		chat.addClientSystemMessage(message);
		//?} else {
		/*chat.addMessage(message);
		 *///?}

		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F));

		if (!MossyLoader.isDevelopmentEnvironment()) {
			InventoryParticlesConfig.getInstance().setTest210(true);
			InventoryParticlesConfig.getInstance().saveAsync();
		}
	}
}
