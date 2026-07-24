package lidwinae.clean.and.clear.glass.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public final class CleanAndClearGlassClient implements ClientModInitializer {
    private static KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        CleanAndClearGlassConfig.get();
        registerKeyMapping();
        VanillaGlassModelPlugin.register();
        AccurateGlassRenderer.register();
    }

    public static Component openSettingsKeyMessage() {
        return openSettingsKey == null
                ? new TextComponent("G")
                : openSettingsKey.getTranslatedKeyMessage();
    }

    private static void registerKeyMapping() {
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.clean-and-clear-glass.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.category.clean-and-clear-glass.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettingsKey.consumeClick()) {
                if (isAltDown(client)) {
                    client.setScreen(new CleanAndClearGlassSettingsScreen(null));
                }
            }
        });
    }

    private static boolean isAltDown(Minecraft client) {
        long window = client.getWindow().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
