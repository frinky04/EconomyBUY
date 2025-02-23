package frinky.economybuy;

import frinky.economybuy.economy.EB_EconomyManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EconomyBUY implements ModInitializer {
	public static final String MOD_ID = "economybuy";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
		EB_Items.initialize();
		EB_Blocks.initialize();
		EB_Entities.initialize();
		EB_Commands.initialize();
		EB_EconomyManager.initialize();
		// Register server start callback
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			EB_EconomyManager.get().syncMarket(server);
		});


	}
}
