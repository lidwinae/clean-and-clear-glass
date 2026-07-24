package lidwinae.clean.and.clear.glass.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
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
    private String tintedGlassStyle = TintedGlassStyle.SUBTLE.serializedName;

    private CleanAndClearGlassConfig() {
    }

    public static CleanAndClearGlassConfig get() {
        if (instance == null) {
            instance = load();
        }

        return instance;
    }

    private static CleanAndClearGlassConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            CleanAndClearGlassConfig config = new CleanAndClearGlassConfig();
            config.save();
            return config;
        }

        try {
            CleanAndClearGlassConfig config = GSON.fromJson(Files.readString(CONFIG_PATH), CleanAndClearGlassConfig.class);

            if (config == null) {
                config = new CleanAndClearGlassConfig();
            }

            config.normalize();
            return config;
        } catch (IOException ignored) {
            return new CleanAndClearGlassConfig();
        }
    }

    public void save() {
        normalize();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException ignored) {
            // A failed config write should not prevent the renderer from using the in-memory setting.
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public GlassStyle glassStyle() {
        return GlassStyle.fromSerializedName(glassStyle);
    }

    public TintedGlassStyle tintedGlassStyle() {
        return TintedGlassStyle.fromSerializedName(tintedGlassStyle);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setGlassStyle(GlassStyle style) {
        glassStyle = style.serializedName;
    }

    public void setTintedGlassStyle(TintedGlassStyle style) {
        tintedGlassStyle = style.serializedName;
    }

    private void normalize() {
        glassStyle = glassStyle().serializedName;
        tintedGlassStyle = tintedGlassStyle().serializedName;
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

    public enum TintedGlassStyle {
        SUBTLE("subtle", "tinted_glass", "tinted_glass_center", "option.clean-and-clear-glass.tinted_glass.subtle"),
        VISIBLE("visible", "tinted_glass_visible", "tinted_glass_center_visible", "option.clean-and-clear-glass.tinted_glass.visible"),
        CLEAR("clear", "tinted_glass_clear", "tinted_glass_center_clear", "option.clean-and-clear-glass.tinted_glass.clear");

        private final String serializedName;
        private final String edgeTexture;
        private final String centerTexture;
        private final String translationKey;

        TintedGlassStyle(String serializedName, String edgeTexture, String centerTexture, String translationKey) {
            this.serializedName = serializedName;
            this.edgeTexture = edgeTexture;
            this.centerTexture = centerTexture;
            this.translationKey = translationKey;
        }

        public String edgeTexture() {
            return edgeTexture;
        }

        public String centerTexture() {
            return centerTexture;
        }

        public String translationKey() {
            return translationKey;
        }

        public TintedGlassStyle next() {
            TintedGlassStyle[] styles = values();
            return styles[(ordinal() + 1) % styles.length];
        }

        private static TintedGlassStyle fromSerializedName(String serializedName) {
            if (serializedName != null) {
                String normalizedName = serializedName.toLowerCase(Locale.ROOT);

                if ("default".equals(normalizedName)) {
                    return SUBTLE;
                }

                for (TintedGlassStyle style : values()) {
                    if (style.serializedName.equals(normalizedName)) {
                        return style;
                    }
                }
            }

            return SUBTLE;
        }
    }
}
