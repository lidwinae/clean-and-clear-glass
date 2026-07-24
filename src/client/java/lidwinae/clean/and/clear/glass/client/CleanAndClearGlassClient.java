package lidwinae.clean.and.clear.glass.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CleanAndClearGlassClient implements ClientModInitializer {
	private static KeyMapping openSettingsKey;

	@Override
	public void onInitializeClient() {
		CleanAndClearGlassConfig.get();
		registerKeyMappings();
		VanillaGlassModelPlugin.register();
	}

	public static Component openSettingsKeyMessage() {
		if (openSettingsKey == null) {
			return Component.literal("G");
		}

		return openSettingsKey.getTranslatedKeyMessage();
	}

	private static void registerKeyMappings() {
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.clean-and-clear-glass.open_tinted_glass_settings",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_G,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("clean-and-clear-glass", "main"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettingsKey.consumeClick()) {
				if (isAltDown(client)) {
					client.setScreenAndShow(new CleanAndClearGlassSettingsScreen(null));
				}
			}
		});
	}

	private static boolean isAltDown(Minecraft client) {
		return InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_LALT)
				|| InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_RALT);
	}
}
