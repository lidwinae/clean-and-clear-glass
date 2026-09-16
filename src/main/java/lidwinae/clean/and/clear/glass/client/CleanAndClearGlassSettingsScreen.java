package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CleanAndClearGlassSettingsScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int ACTION_BUTTON_WIDTH = 98;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXT_WIDTH = 260;

    private static final int BACKGROUND_DARK_OVERLAY = 0x70000000;

    private final Screen parent;

    private boolean selectedEnabled;
    private CleanAndClearGlassConfig.GlassStyle selectedGlassStyle;
    private CleanAndClearGlassConfig.TintedGlassStyle selectedTintedStyle;

    private Button enabledButton;
    private Button glassStyleButton;
    private Button tintedStyleButton;

    public CleanAndClearGlassSettingsScreen(Screen parent) {
        super(Component.translatable(
                "screen.cleanandclearglass.settings"
        ));

        this.parent = parent;
        this.selectedEnabled = CleanAndClearGlassConfig.enabled();
        this.selectedGlassStyle =
                CleanAndClearGlassConfig.glassStyle();
        this.selectedTintedStyle =
                CleanAndClearGlassConfig.tintedGlassStyle();
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
                    tintedStyleButton.active = selectedEnabled;
                }
        ).bounds(
                centerX - BUTTON_WIDTH / 2,
                top + 34,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());

        glassStyleButton = addRenderableWidget(Button.builder(
                glassStyleMessage(),
                button -> {
                    selectedGlassStyle = selectedGlassStyle.next();
                    glassStyleButton.setMessage(glassStyleMessage());
                }
        ).bounds(
                centerX - BUTTON_WIDTH / 2,
                top + 58,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());

        tintedStyleButton = addRenderableWidget(Button.builder(
                tintedStyleMessage(),
                button -> {
                    selectedTintedStyle = selectedTintedStyle.next();
                    tintedStyleButton.setMessage(tintedStyleMessage());
                }
        ).bounds(
                centerX - BUTTON_WIDTH / 2,
                top + 82,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());

        glassStyleButton.active = selectedEnabled;
        tintedStyleButton.active = selectedEnabled;

        addRenderableOnly(new MultiLineTextWidget(
                centerX - TEXT_WIDTH / 2,
                top + 116,
                keyBindMessage(),
                font
        ).setMaxWidth(TEXT_WIDTH).setCentered(true));

        addRenderableOnly(new MultiLineTextWidget(
                centerX - TEXT_WIDTH / 2,
                top + 130,
                Component.translatable(
                        "option.cleanandclearglass.change_key_in_controls"
                ),
                font
        ).setMaxWidth(TEXT_WIDTH).setCentered(true));

        addRenderableWidget(Button.builder(
                Component.translatable(
                        "button.cleanandclearglass.apply_reload"
                ),
                button -> applySettings()
        ).bounds(
                centerX - ACTION_BUTTON_WIDTH - 4,
                top + 164,
                ACTION_BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> onClose()
        ).bounds(
                centerX + 4,
                top + 164,
                ACTION_BUTTON_WIDTH,
                BUTTON_HEIGHT
        ).build());
    }

    private void applySettings() {
        CleanAndClearGlassConfig.ENABLED.set(selectedEnabled);
        CleanAndClearGlassConfig.GLASS_STYLE.set(
                selectedGlassStyle
        );
        CleanAndClearGlassConfig.TINTED_GLASS_STYLE.set(
                selectedTintedStyle
        );
        CleanAndClearGlassConfig.save();

        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);

            // Model wrapper dan sprite dipilih kembali berdasarkan config.
            minecraft.reloadResourcePacks();
        }
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.fill(
                0,
                0,
                width,
                height,
                BACKGROUND_DARK_OVERLAY
        );
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    private Component enabledMessage() {
        Component state = Component.translatable(
                selectedEnabled
                        ? "option.cleanandclearglass.enabled.on"
                        : "option.cleanandclearglass.enabled.off"
        );

        return Component.translatable(
                "option.cleanandclearglass.enabled",
                state
        );
    }

    private Component glassStyleMessage() {
        return Component.translatable(
                "option.cleanandclearglass.glass",
                Component.translatable(
                        selectedGlassStyle.translationKey()
                )
        );
    }

    private Component tintedStyleMessage() {
        return Component.translatable(
                "option.cleanandclearglass.tinted_glass",
                Component.translatable(
                        selectedTintedStyle.translationKey()
                )
        );
    }

    private static Component keyBindMessage() {
        return Component.translatable(
                "option.cleanandclearglass.key_bind",
                CleanAndClearGlassClient.openSettingsKeyMessage()
        );
    }
}
