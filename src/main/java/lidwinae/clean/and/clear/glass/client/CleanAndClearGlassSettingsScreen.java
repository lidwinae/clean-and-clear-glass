package lidwinae.clean.and.clear.glass.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public final class CleanAndClearGlassSettingsScreen extends Screen {
    private final Screen parent;
    private boolean selectedEnabled;
    private CleanAndClearGlassConfig.GlassStyle selectedGlassStyle;
    private Button enabledButton;
    private Button glassStyleButton;

    public CleanAndClearGlassSettingsScreen(Screen parent) {
        super(new TranslatableComponent("screen.clean-and-clear-glass.settings"));
        this.parent = parent;
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        this.selectedEnabled = config.enabled();
        this.selectedGlassStyle = config.glassStyle();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int top = height / 2 - 70;

        enabledButton = addButton(new Button(
                centerX - 100,
                top + 30,
                200,
                20,
                enabledMessage(),
                button -> {
                    selectedEnabled = !selectedEnabled;
                    enabledButton.setMessage(enabledMessage());
                    glassStyleButton.active = selectedEnabled;
                }
        ));

        glassStyleButton = addButton(new Button(
                centerX - 100,
                top + 54,
                200,
                20,
                glassStyleMessage(),
                button -> {
                    selectedGlassStyle = selectedGlassStyle.next();
                    glassStyleButton.setMessage(glassStyleMessage());
                }
        ));
        glassStyleButton.active = selectedEnabled;

        addButton(new Button(
                centerX - 100,
                top + 120,
                98,
                20,
                new TranslatableComponent("button.clean-and-clear-glass.apply_reload"),
                button -> applyAndClose()
        ));

        addButton(new Button(
                centerX + 2,
                top + 120,
                98,
                20,
                new TranslatableComponent("gui.cancel"),
                button -> onClose()
        ));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int centerX = width / 2;
        int top = height / 2 - 70;

        renderBackground(poseStack);

        drawCenteredString(
                poseStack,
                font,
                title,
                centerX,
                top,
                0xFFFFFF
        );

        Component key = new TranslatableComponent(
                "option.clean-and-clear-glass.key_bind",
                CleanAndClearGlassClient.openSettingsKeyMessage()
        );

        drawCenteredString(
                poseStack,
                font,
                key,
                centerX,
                top + 87,
                0xA0A0A0
        );

        Component changeKeyHint = new TranslatableComponent(
                "option.clean-and-clear-glass.change_key_in_controls"
        );

        drawCenteredString(
                poseStack,
                font,
                changeKeyHint,
                centerX,
                top + 99,
                0xA0A0A0
        );

        super.render(poseStack, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void applyAndClose() {
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        config.setEnabled(selectedEnabled);
        config.setGlassStyle(selectedGlassStyle);
        config.save();
        AccurateGlassRenderer.clear();

        if (minecraft != null) {
            minecraft.setScreen(parent);
            minecraft.reloadResourcePacks();
        }
    }

    private Component enabledMessage() {
        return new TranslatableComponent(
                "option.clean-and-clear-glass.enabled",
                new TranslatableComponent(selectedEnabled
                        ? "option.clean-and-clear-glass.enabled.on"
                        : "option.clean-and-clear-glass.enabled.off")
        );
    }

    private Component glassStyleMessage() {
        return new TranslatableComponent(
                "option.clean-and-clear-glass.glass",
                new TranslatableComponent(selectedGlassStyle.translationKey())
        );
    }
}
