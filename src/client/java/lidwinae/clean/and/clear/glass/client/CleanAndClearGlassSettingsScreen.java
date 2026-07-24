package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CleanAndClearGlassSettingsScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int ACTION_BUTTON_WIDTH = 98;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXT_WIDTH = 260;
    private static final int BACKGROUND_DARK_OVERLAY = 0x50000000;

    private final Screen parent;
    private boolean selectedEnabled;
    private CleanAndClearGlassConfig.GlassStyle selectedGlassStyle;
    private CleanAndClearGlassConfig.TintedGlassStyle selectedTintedGlassStyle;
    private Button enabledButton;
    private Button glassStyleButton;
    private Button tintedGlassButton;

    public CleanAndClearGlassSettingsScreen(Screen parent) {
        super(Component.translatable("screen.clean-and-clear-glass.settings"));
        this.parent = parent;
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        this.selectedEnabled = config.enabled();
        this.selectedGlassStyle = config.glassStyle();
        this.selectedTintedGlassStyle = config.tintedGlassStyle();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 98;

        addRenderableOnly(new MultiLineTextWidget(
                centerX - TEXT_WIDTH / 2,
                top,
                title,
                font
        ).setMaxWidth(TEXT_WIDTH).setCentered(true));

        enabledButton = addRenderableWidget(Button.builder(
                enabledMessage(),
                button -> {
                    selectedEnabled = !selectedEnabled;
                    enabledButton.setMessage(enabledMessage());
                    glassStyleButton.active = selectedEnabled;
                    tintedGlassButton.active = selectedEnabled;
                }
        ).bounds(centerX - BUTTON_WIDTH / 2, top + 34, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        glassStyleButton = addRenderableWidget(Button.builder(
                glassStyleMessage(),
                button -> {
                    selectedGlassStyle = selectedGlassStyle.next();
                    glassStyleButton.setMessage(glassStyleMessage());
                }
        ).bounds(centerX - BUTTON_WIDTH / 2, top + 58, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        glassStyleButton.active = selectedEnabled;

        tintedGlassButton = addRenderableWidget(Button.builder(
                tintedGlassMessage(),
                button -> {
                    selectedTintedGlassStyle = selectedTintedGlassStyle.next();
                    tintedGlassButton.setMessage(tintedGlassMessage());
                }
        ).bounds(centerX - BUTTON_WIDTH / 2, top + 82, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        tintedGlassButton.active = selectedEnabled;

        addRenderableOnly(new MultiLineTextWidget(
                centerX - TEXT_WIDTH / 2,
                top + 116,
                keyBindMessage(),
                font
        ).setMaxWidth(TEXT_WIDTH).setCentered(true));

        addRenderableOnly(new MultiLineTextWidget(
                centerX - TEXT_WIDTH / 2,
                top + 130,
                Component.translatable("option.clean-and-clear-glass.change_key_in_controls"),
                font
        ).setMaxWidth(TEXT_WIDTH).setCentered(true));

        addRenderableWidget(Button.builder(
                Component.translatable("button.clean-and-clear-glass.apply_reload"),
                button -> {
                    CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
                    config.setEnabled(selectedEnabled);
                    config.setGlassStyle(selectedGlassStyle);
                    config.setTintedGlassStyle(selectedTintedGlassStyle);
                    config.save();

                    if (minecraft != null) {
                        minecraft.setScreenAndShow(parent);
                        minecraft.reloadResourcePacks();
                    }
                }
        ).bounds(centerX - ACTION_BUTTON_WIDTH - 4, top + 164, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> onClose()
        ).bounds(centerX + 4, top + 164, ACTION_BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, width, height, BACKGROUND_DARK_OVERLAY);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    private Component enabledMessage() {
        String translationKey = selectedEnabled
                ? "option.clean-and-clear-glass.enabled.on"
                : "option.clean-and-clear-glass.enabled.off";

        return Component.translatable(
                "option.clean-and-clear-glass.enabled",
                Component.translatable(translationKey)
        );
    }

    private Component glassStyleMessage() {
        return Component.translatable(
                "option.clean-and-clear-glass.glass",
                Component.translatable(selectedGlassStyle.translationKey())
        );
    }

    private Component tintedGlassMessage() {
        return Component.translatable(
                "option.clean-and-clear-glass.tinted_glass",
                Component.translatable(selectedTintedGlassStyle.translationKey())
        );
    }

    private static Component keyBindMessage() {
        return Component.translatable(
                "option.clean-and-clear-glass.key_bind",
                CleanAndClearGlassClient.openSettingsKeyMessage()
        );
    }
}
