package lidwinae.clean.and.clear.glass.client;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public final class CleanAndClearGlassModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return CleanAndClearGlassSettingsScreen::new;
	}
}