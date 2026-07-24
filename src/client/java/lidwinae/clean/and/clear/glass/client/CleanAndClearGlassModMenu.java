package lidwinae.clean.and.clear.glass.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class CleanAndClearGlassModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CleanAndClearGlassSettingsScreen::new;
    }
}
