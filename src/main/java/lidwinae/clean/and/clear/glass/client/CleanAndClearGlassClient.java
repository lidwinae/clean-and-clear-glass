package lidwinae.clean.and.clear.glass.client;

import com.mojang.blaze3d.platform.InputConstants;
import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CleanandClearGlass.MODID, dist = Dist.CLIENT)
public final class CleanAndClearGlassClient {
    private static final String CATEGORY =
            "key.category.cleanandclearglass.main";

    private static final KeyMapping OPEN_SETTINGS =
            new KeyMapping(
                    "key.cleanandclearglass.open_settings",
                    KeyConflictContext.IN_GAME,
                    KeyModifier.ALT,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    CATEGORY
            );

    public CleanAndClearGlassClient(
            IEventBus modBus,
            ModContainer container
    ) {
        container.registerConfig(
                ModConfig.Type.CLIENT,
                CleanAndClearGlassConfig.SPEC,
                "cleanandclearglass-client.toml"
        );

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (IConfigScreenFactory) (ignoredContainer, parent) ->
                        new CleanAndClearGlassSettingsScreen(parent)
        );

        modBus.addListener(this::registerKeyMappings);
        modBus.addListener(GlassModelHandler::registerAdditionalModels);
        modBus.addListener(GlassModelHandler::modifyBakingResult);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void registerKeyMappings(
            RegisterKeyMappingsEvent event
    ) {
        event.register(OPEN_SETTINGS);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        while (OPEN_SETTINGS.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(
                        new CleanAndClearGlassSettingsScreen(null)
                );
            }
        }
    }

    public static Component openSettingsKeyMessage() {
        return OPEN_SETTINGS.getTranslatedKeyMessage();
    }
}
