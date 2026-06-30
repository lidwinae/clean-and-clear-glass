package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.api.ClientModInitializer;

public class CleanAndClearGlassClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register client-side model wrappers for vanilla glass and glass panes.
		VanillaGlassModelPlugin.register();
	}
}
