package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VanillaGlassModelPlugin {
    private static final String MOD_ID = "clean-and-clear-glass";

    private static final Set<Block> CONNECTED_GLASS_BLOCKS = Set.of(
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

    private static final Set<Block> CONNECTED_GLASS_PANES = Set.of(
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

    private static final Map<ModelResourceLocation, Block> CONNECTED_BLOCK_MODELS = connectedBlockModels();
    private static final Map<ModelResourceLocation, ResourceLocation> CLEAN_ITEM_MODELS = cleanItemModels();

    private static final ModelResourceLocation TINTED_GLASS_ITEM_ID = inventoryModel(
            BuiltInRegistries.BLOCK.getKey(Blocks.TINTED_GLASS)
    );
    private static final ResourceLocation SUBTLE_TINTED_GLASS_ITEM_MODEL_ID = modModel("block/tinted_glass_item");
    private static final ResourceLocation VISIBLE_TINTED_GLASS_ITEM_MODEL_ID = modModel("block/tinted_glass_visible_item");
    private static final ResourceLocation CLEAR_TINTED_GLASS_ITEM_MODEL_ID = modModel("block/tinted_glass_clear_item");
    private static final Set<ResourceLocation> EXTRA_ITEM_MODELS = extraItemModels();

    private VanillaGlassModelPlugin() {
    }

    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(EXTRA_ITEM_MODELS);

            pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
                if (model == null || context.id() == null) {
                    return model;
                }

                ResourceLocation modelId = context.id();
                Block block = CONNECTED_BLOCK_MODELS.get(modelId);

                if (block != null) {
                    return wrapBlockModel(model, block, context);
                }

                if (TINTED_GLASS_ITEM_ID.equals(modelId)) {
                    return new CleanGlassItemModel(
                            model,
                            bakeExtraModel(context, SUBTLE_TINTED_GLASS_ITEM_MODEL_ID),
                            bakeExtraModel(context, VISIBLE_TINTED_GLASS_ITEM_MODEL_ID),
                            bakeExtraModel(context, CLEAR_TINTED_GLASS_ITEM_MODEL_ID)
                    );
                }

                ResourceLocation cleanModelId = CLEAN_ITEM_MODELS.get(modelId);

                if (cleanModelId != null) {
                    return new CleanGlassItemModel(model, bakeExtraModel(context, cleanModelId));
                }

                return model;
            });
        });
    }

    private static BakedModel wrapBlockModel(
            BakedModel model,
            Block block,
            ModelModifier.AfterBake.Context context
    ) {
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();

        if (!config.enabled()) {
            return model;
        }

        if (block == Blocks.TINTED_GLASS) {
            CleanAndClearGlassConfig.TintedGlassStyle style = config.tintedGlassStyle();
            TextureAtlasSprite edgeMaterial = context.textureGetter().apply(textureMaterial(style.edgeTexture()));
            TextureAtlasSprite centerMaterial = context.textureGetter().apply(textureMaterial(style.centerTexture()));

            return new VanillaConnectedGlassModel(model, edgeMaterial, centerMaterial, true, true);
        }

        TextureAtlasSprite edgeMaterial = context.textureGetter().apply(textureMaterial(glassTextureName(block)));

        if (CONNECTED_GLASS_PANES.contains(block)) {
            if (block == Blocks.GLASS_PANE && config.glassStyle().centerTexture() != null) {
                TextureAtlasSprite centerMaterial = context.textureGetter().apply(
                        textureMaterial(config.glassStyle().centerTexture())
                );
                return new VanillaConnectedGlassPaneModel(model, edgeMaterial, centerMaterial, false);
            }

            return new VanillaConnectedGlassPaneModel(model, edgeMaterial, block != Blocks.GLASS_PANE);
        }

        if (block == Blocks.GLASS && config.glassStyle().centerTexture() != null) {
            TextureAtlasSprite centerMaterial = context.textureGetter().apply(
                    textureMaterial(config.glassStyle().centerTexture())
            );
            return new VanillaConnectedGlassModel(model, edgeMaterial, centerMaterial, true, false);
        }

        return new VanillaConnectedGlassModel(model, edgeMaterial, block != Blocks.GLASS);
    }

    private static BakedModel bakeExtraModel(
            ModelModifier.AfterBake.Context context,
            ResourceLocation modelId
    ) {
        return context.baker().bake(modelId, context.settings());
    }

    private static Material textureMaterial(String textureName) {
        return new Material(
                InventoryMenu.BLOCK_ATLAS,
                new ResourceLocation(MOD_ID, "block/" + textureName)
        );
    }

    private static ResourceLocation modModel(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private static ModelResourceLocation inventoryModel(ResourceLocation id) {
        return new ModelResourceLocation(id, "inventory");
    }

    private static Map<ModelResourceLocation, Block> connectedBlockModels() {
        Map<ModelResourceLocation, Block> models = new HashMap<>();
        addBlockModels(models, CONNECTED_GLASS_BLOCKS);
        addBlockModels(models, CONNECTED_GLASS_PANES);
        return Map.copyOf(models);
    }

    private static void addBlockModels(Map<ModelResourceLocation, Block> models, Set<Block> blocks) {
        for (Block block : blocks) {
            block.getStateDefinition().getPossibleStates().forEach(state ->
                    models.put(BlockModelShaper.stateToModelLocation(state), block)
            );
        }
    }

    private static Map<ModelResourceLocation, ResourceLocation> cleanItemModels() {
        Map<ModelResourceLocation, ResourceLocation> models = new HashMap<>();

        for (Block block : CONNECTED_GLASS_BLOCKS) {
            if (block != Blocks.TINTED_GLASS) {
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                models.put(
                        inventoryModel(blockId),
                        modModel("block/" + blockId.getPath() + "_item")
                );
            }
        }

        for (Block block : CONNECTED_GLASS_PANES) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            models.put(
                    inventoryModel(blockId),
                    modModel("item/" + blockId.getPath() + "_item")
            );
        }

        return Map.copyOf(models);
    }

    private static Set<ResourceLocation> extraItemModels() {
        Set<ResourceLocation> models = new HashSet<>(CLEAN_ITEM_MODELS.values());
        models.add(SUBTLE_TINTED_GLASS_ITEM_MODEL_ID);
        models.add(VISIBLE_TINTED_GLASS_ITEM_MODEL_ID);
        models.add(CLEAR_TINTED_GLASS_ITEM_MODEL_ID);
        return Set.copyOf(models);
    }

    private static String glassTextureName(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return path.endsWith("_pane") ? path.substring(0, path.length() - "_pane".length()) : path;
    }
}
