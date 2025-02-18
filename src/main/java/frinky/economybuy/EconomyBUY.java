package frinky.economybuy;

import net.fabricmc.api.ModInitializer;
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


	}
}
