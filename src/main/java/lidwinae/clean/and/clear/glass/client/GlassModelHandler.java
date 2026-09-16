package lidwinae.clean.and.clear.glass.client;

import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Set;
import java.util.function.Function;

public final class GlassModelHandler {
    private static final Set<Block> GLASS_BLOCKS = Set.of(
            Blocks.GLASS,
            Blocks.TINTED_GLASS,
            Blocks.WHITE_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS
    );

    private static final Set<Block> GLASS_PANES = Set.of(
            Blocks.GLASS_PANE,
            Blocks.WHITE_STAINED_GLASS_PANE,
            Blocks.ORANGE_STAINED_GLASS_PANE,
            Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
            Blocks.YELLOW_STAINED_GLASS_PANE,
            Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS_PANE,
            Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS_PANE,
            Blocks.PURPLE_STAINED_GLASS_PANE,
            Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.GREEN_STAINED_GLASS_PANE,
            Blocks.RED_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS_PANE
    );

    private GlassModelHandler() {
    }

    public static void registerStandaloneModels(
            ModelEvent.RegisterStandalone event
    ) {
        CleanItemModels.register(event);
    }

    public static void modifyBakingResult(
            ModelEvent.ModifyBakingResult event
    ) {
        // Item wrapper selalu dipasang agar OFF dapat kembali ke vanilla.
        CleanItemModels.replace(event);

        if (!CleanAndClearGlassConfig.enabled()) {
            return;
        }

        Function<Identifier, TextureAtlasSprite> sprites =
                event.getTextureGetter();

        event.getBakingResult()
                .blockStateModels()
                .replaceAll((state, original) -> {
                    Block block = state.getBlock();

                    if (GLASS_BLOCKS.contains(block)) {
                        if (block == Blocks.TINTED_GLASS) {
                            CleanAndClearGlassConfig
                                    .TintedGlassStyle style =
                                    CleanAndClearGlassConfig
                                            .tintedGlassStyle();

                            return new VanillaConnectedGlassModel(
                                    original,
                                    sprite(
                                            sprites,
                                            style.edgeTexture()
                                    ),
                                    sprite(
                                            sprites,
                                            style.centerTexture()
                                    ),
                                    true
                            );
                        }

                        TextureAtlasSprite edge = sprite(
                                sprites,
                                textureName(block)
                        );

                        if (block == Blocks.GLASS
                                && CleanAndClearGlassConfig
                                .glassStyle()
                                .centerTexture() != null) {
                            return new VanillaConnectedGlassModel(
                                    original,
                                    edge,
                                    sprite(
                                            sprites,
                                            CleanAndClearGlassConfig
                                                    .glassStyle()
                                                    .centerTexture()
                                    ),
                                    true
                            );
                        }

                        return new VanillaConnectedGlassModel(
                                original,
                                edge
                        );
                    }

                    if (GLASS_PANES.contains(block)) {
                        TextureAtlasSprite edge = sprite(
                                sprites,
                                textureName(block)
                        );

                        if (block == Blocks.GLASS_PANE
                                && CleanAndClearGlassConfig
                                .glassStyle()
                                .centerTexture() != null) {
                            return new VanillaConnectedGlassPaneModel(
                                    original,
                                    edge,
                                    sprite(
                                            sprites,
                                            CleanAndClearGlassConfig
                                                    .glassStyle()
                                                    .centerTexture()
                                    )
                            );
                        }

                        return new VanillaConnectedGlassPaneModel(
                                original,
                                edge
                        );
                    }

                    return original;
                });
    }

    private static TextureAtlasSprite sprite(
            Function<Identifier, TextureAtlasSprite> getter,
            String textureName
    ) {
        return getter.apply(
                Identifier.fromNamespaceAndPath(
                        CleanandClearGlass.MODID,
                        "block/" + textureName
                )
        );
    }

    private static String textureName(Block block) {
        String path = BuiltInRegistries.BLOCK
                .getKey(block)
                .getPath();

        if (path.endsWith("_pane")) {
            return path.substring(
                    0,
                    path.length() - "_pane".length()
            );
        }

        return path;
    }
}
