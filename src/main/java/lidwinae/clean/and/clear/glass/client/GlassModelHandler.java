package lidwinae.clean.and.clear.glass.client;

import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;
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

    public static void registerAdditionalModels(
            ModelEvent.RegisterAdditional event
    ) {
        CleanItemModels.register(event);
    }

    public static void modifyBakingResult(
            ModelEvent.ModifyBakingResult event
    ) {
        if (!CleanAndClearGlassConfig.enabled()) {
            return;
        }

        CleanItemModels.replace(event);

        Map<ModelResourceLocation, BakedModel> models =
                event.getModels();

        Function<Material, TextureAtlasSprite> sprites =
                event.getTextureGetter();

        for (Block block : GLASS_BLOCKS) {
            for (BlockState state
                    : block.getStateDefinition()
                    .getPossibleStates()) {
                ModelResourceLocation location =
                        BlockModelShaper.stateToModelLocation(state);

                BakedModel original = models.get(location);

                if (original != null) {
                    models.put(
                            location,
                            connectedBlockModel(
                                    original,
                                    block,
                                    sprites
                            )
                    );
                }
            }
        }

        for (Block block : GLASS_PANES) {
            for (BlockState state
                    : block.getStateDefinition()
                    .getPossibleStates()) {
                ModelResourceLocation location =
                        BlockModelShaper.stateToModelLocation(state);

                BakedModel original = models.get(location);

                if (original != null) {
                    models.put(
                            location,
                            connectedPaneModel(
                                    original,
                                    block,
                                    sprites
                            )
                    );
                }
            }
        }
    }

    private static BakedModel connectedBlockModel(
            BakedModel original,
            Block block,
            Function<Material, TextureAtlasSprite> sprites
    ) {
        if (block == Blocks.TINTED_GLASS) {
            CleanAndClearGlassConfig.TintedGlassStyle style =
                    CleanAndClearGlassConfig.tintedGlassStyle();

            return new VanillaConnectedGlassModel(
                    original,
                    sprite(sprites, style.edgeTexture()),
                    sprite(sprites, style.centerTexture()),
                    true
            );
        }

        TextureAtlasSprite edge =
                sprite(sprites, textureName(block));

        String centerTexture = block == Blocks.GLASS
                ? CleanAndClearGlassConfig
                .glassStyle()
                .centerTexture()
                : null;

        if (centerTexture != null) {
            return new VanillaConnectedGlassModel(
                    original,
                    edge,
                    sprite(sprites, centerTexture),
                    true
            );
        }

        return new VanillaConnectedGlassModel(
                original,
                edge
        );
    }

    private static BakedModel connectedPaneModel(
            BakedModel original,
            Block block,
            Function<Material, TextureAtlasSprite> sprites
    ) {
        TextureAtlasSprite edge =
                sprite(sprites, textureName(block));

        String centerTexture = block == Blocks.GLASS_PANE
                ? CleanAndClearGlassConfig
                .glassStyle()
                .centerTexture()
                : null;

        if (centerTexture != null) {
            return new VanillaConnectedGlassPaneModel(
                    original,
                    edge,
                    sprite(sprites, centerTexture)
            );
        }

        return new VanillaConnectedGlassPaneModel(
                original,
                edge
        );
    }

    private static TextureAtlasSprite sprite(
            Function<Material, TextureAtlasSprite> getter,
            String textureName
    ) {
        ResourceLocation texture =
                ResourceLocation.fromNamespaceAndPath(
                        CleanandClearGlass.MODID,
                        "block/" + textureName
                );

        return getter.apply(
                new Material(
                        TextureAtlas.LOCATION_BLOCKS,
                        texture
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
