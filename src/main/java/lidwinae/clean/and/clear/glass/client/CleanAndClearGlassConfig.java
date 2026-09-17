package lidwinae.clean.and.clear.glass.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CleanAndClearGlassConfig {
    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enables Clean and Clear Glass rendering.")
            .define("enabled", true);

    public static final ModConfigSpec.EnumValue<GlassStyle> GLASS_STYLE =
            BUILDER
                    .comment("Rendering style used for regular glass.")
                    .defineEnum("glassStyle", GlassStyle.CLEAR);

    public static final ModConfigSpec.EnumValue<TintedGlassStyle>
            TINTED_GLASS_STYLE = BUILDER
            .comment("Rendering style used for tinted glass.")
            .defineEnum(
                    "tintedGlassStyle",
                    TintedGlassStyle.SUBTLE
            );

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CleanAndClearGlassConfig() {
    }

    public static boolean enabled() {
        return ENABLED.getAsBoolean();
    }

    public static GlassStyle glassStyle() {
        return GLASS_STYLE.get();
    }

    public static TintedGlassStyle tintedGlassStyle() {
        return TINTED_GLASS_STYLE.get();
    }

    public static void save() {
        SPEC.save();
    }

    public enum GlassStyle {
        CLEAR(
                null,
                "option.cleanandclearglass.glass.clear"
        ),
        SUBTLE(
                "glass_center_subtle",
                "option.cleanandclearglass.glass.subtle"
        );

        private final String centerTexture;
        private final String translationKey;

        GlassStyle(
                String centerTexture,
                String translationKey
        ) {
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
            GlassStyle[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public enum TintedGlassStyle {
        SUBTLE(
                "tinted_glass",
                "tinted_glass_center",
                "option.cleanandclearglass.tinted_glass.subtle"
        ),
        VISIBLE(
                "tinted_glass_visible",
                "tinted_glass_center_visible",
                "option.cleanandclearglass.tinted_glass.visible"
        ),
        CLEAR(
                "tinted_glass_clear",
                "tinted_glass_center_clear",
                "option.cleanandclearglass.tinted_glass.clear"
        );

        private final String edgeTexture;
        private final String centerTexture;
        private final String translationKey;

        TintedGlassStyle(
                String edgeTexture,
                String centerTexture,
                String translationKey
        ) {
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
            TintedGlassStyle[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}