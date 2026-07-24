package lidwinae.clean.and.clear.glass.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CleanAndClearGlassConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("clean-and-clear-glass.json");

    private static CleanAndClearGlassConfig instance;
    private boolean enabled = true;
    private String glassStyle = GlassStyle.CLEAR.serializedName;

    private CleanAndClearGlassConfig() {
    }

    public static CleanAndClearGlassConfig get() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    public boolean enabled() {
        return enabled;
    }

    public GlassStyle glassStyle() {
        return GlassStyle.fromSerializedName(glassStyle);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGlassStyle(GlassStyle style) {
        glassStyle = style.serializedName;
    }

    public void save() {
        normalize();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            // Keep using the in-memory value if the config file cannot be written.
        }
    }

    private static CleanAndClearGlassConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            CleanAndClearGlassConfig config = new CleanAndClearGlassConfig();
            config.save();
            return config;
        }

        try {
            String json = new String(Files.readAllBytes(CONFIG_PATH), StandardCharsets.UTF_8);
            CleanAndClearGlassConfig config = GSON.fromJson(json, CleanAndClearGlassConfig.class);

            if (config == null) {
                config = new CleanAndClearGlassConfig();
            }

            config.normalize();
            return config;
        } catch (IOException ignored) {
            return new CleanAndClearGlassConfig();
        }
    }

    private void normalize() {
        glassStyle = glassStyle().serializedName;
    }

    public enum GlassStyle {
        CLEAR("clear", null, "option.clean-and-clear-glass.glass.clear"),
        SUBTLE("subtle", "glass_center_subtle", "option.clean-and-clear-glass.glass.subtle");

        private final String serializedName;
        private final String centerTexture;
        private final String translationKey;

        GlassStyle(String serializedName, String centerTexture, String translationKey) {
            this.serializedName = serializedName;
            this.centerTexture = centerTexture;
            this.translationKey = translationKey;
        }

        public String centerTexture() {
            return centerTexture;
        }

        public String translationKey() {
            return translationKey;
        }

        public GlassStyle next() {
            GlassStyle[] styles = values();
            return styles[(ordinal() + 1) % styles.length];
        }

        private static GlassStyle fromSerializedName(String serializedName) {
            if (serializedName != null) {
                String normalizedName = serializedName.toLowerCase(Locale.ROOT);

                for (GlassStyle style : values()) {
                    if (style.serializedName.equals(normalizedName)) {
                        return style;
                    }
                }
            }

            return CLEAR;
        }
    }
}
