package net.lopymine.mossy.client;

import org.slf4j.*;

import net.fabricmc.api.ClientModInitializer;

import net.lopymine.mossy.Mossy;

public class MossyClient implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger(Mossy.MOD_NAME + "/Client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("{} Client Initialized", Mossy.MOD_NAME);
	}
}
