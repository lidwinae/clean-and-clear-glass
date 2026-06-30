package lidwinae.clean.and.clear.glass;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanAndClearGlass implements ModInitializer {
	public static final String MOD_ID = "clean-and-clear-glass";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Keep the common initializer lightweight; all rendering work is client-only.
		LOGGER.info("Clean and Clear Glass by lidwinae");
	}
}
