package lidwinae.clean.and.clear.glass.client;

import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class GlassModelHandler {
    private static final Set<Block> GLASS_BLOCKS =
            connectedGlassBlocks();

    private static final Set<Block> GLASS_PANES =
            connectedGlassPanes();

    private GlassModelHandler() {
    }

    private static Set<Block> connectedGlassBlocks() {
        Set<Block> blocks =
                new HashSet<>(Blocks.STAINED_GLASS.asList());

        blocks.add(Blocks.GLASS);
        blocks.add(Blocks.TINTED_GLASS);

        return Set.copyOf(blocks);
    }

    private static Set<Block> connectedGlassPanes() {
        Set<Block> panes =
                new HashSet<>(Blocks.STAINED_GLASS_PANE.asList());

        panes.add(Blocks.GLASS_PANE);

        return Set.copyOf(panes);
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
