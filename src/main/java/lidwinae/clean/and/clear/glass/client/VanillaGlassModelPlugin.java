package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VanillaGlassModelPlugin {
    private static final String MOD_ID = "clean-and-clear-glass";
    private static final ResourceLocation GLASS_CENTER_SUBTLE_TEXTURE =
            new ResourceLocation(MOD_ID, "block/glass_center_subtle");

    private static final Set<Block> CONNECTED_GLASS_BLOCKS = immutableSet(
            Blocks.GLASS,
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

    private static final Set<Block> CONNECTED_GLASS_PANES = immutableSet(
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

    private static final Map<ModelResourceLocation, Block> CONNECTED_MODELS = connectedModels();
    private static final Map<ModelResourceLocation, ResourceLocation> CLEAN_ITEM_MODELS = cleanItemModels();

    private VanillaGlassModelPlugin() {
    }

    public static void register() {
        // Minecraft 1.16.5 only stitches sprites that are referenced by a model
        // or explicitly registered. The subtle center is loaded dynamically,
        // so it must be added to the block atlas before model baking starts.
        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register(
                (atlas, registry) -> registry.register(GLASS_CENTER_SUBTLE_TEXTURE)
        );

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, output) -> {
            for (ResourceLocation model : CLEAN_ITEM_MODELS.values()) {
                output.accept(model);
            }
        });
    }

    public static Map<ResourceLocation, BakedModel> wrapModels(
            ModelManager manager,
            Map<ResourceLocation, BakedModel> bakedModels
    ) {
        Map<ResourceLocation, BakedModel> wrappedModels = new HashMap<ResourceLocation, BakedModel>(bakedModels);
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        boolean enabled = config.enabled();

        if (enabled) {
            for (Map.Entry<ModelResourceLocation, Block> entry : CONNECTED_MODELS.entrySet()) {
                BakedModel original = bakedModels.get(entry.getKey());

                if (original == null) {
                    continue;
                }

                Block block = entry.getValue();
                TextureAtlasSprite edgeTexture = manager
                        .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                        .getSprite(new ResourceLocation(MOD_ID, "block/" + glassTextureName(block)));
                boolean accurate = block != Blocks.GLASS && block != Blocks.GLASS_PANE;
                String centerTextureName = block == Blocks.GLASS || block == Blocks.GLASS_PANE
                        ? config.glassStyle().centerTexture()
                        : null;
                TextureAtlasSprite centerTexture = centerTextureName == null
                        ? null
                        : manager.getAtlas(TextureAtlas.LOCATION_BLOCKS)
                                .getSprite(new ResourceLocation(MOD_ID, "block/" + centerTextureName));
                BakedModel connected;

                if (CONNECTED_GLASS_PANES.contains(block)) {
                    connected = new VanillaConnectedGlassPaneModel(
                            original,
                            edgeTexture,
                            centerTexture,
                            accurate
                    );
                } else if (centerTexture != null) {
                    connected = new VanillaConnectedGlassModel(
                            original,
                            edgeTexture,
                            centerTexture,
                            true,
                            accurate
                    );
                } else {
                    connected = new VanillaConnectedGlassModel(original, edgeTexture, accurate);
                }

                wrappedModels.put(entry.getKey(), connected);
            }
        }

        for (Map.Entry<ModelResourceLocation, ResourceLocation> entry : CLEAN_ITEM_MODELS.entrySet()) {
            BakedModel original = bakedModels.get(entry.getKey());
            BakedModel clean = bakedModels.get(entry.getValue());

            if (original != null && clean != null) {
                wrappedModels.put(entry.getKey(), new CleanGlassItemModel(original, clean));
            }
        }

        return wrappedModels;
    }

    private static Map<ModelResourceLocation, Block> connectedModels() {
        Map<ModelResourceLocation, Block> models = new HashMap<ModelResourceLocation, Block>();
        addBlockModels(models, CONNECTED_GLASS_BLOCKS);
        addBlockModels(models, CONNECTED_GLASS_PANES);
        return Collections.unmodifiableMap(models);
    }

    private static void addBlockModels(Map<ModelResourceLocation, Block> models, Set<Block> blocks) {
        for (Block block : blocks) {
            for (net.minecraft.world.level.block.state.BlockState state : block.getStateDefinition().getPossibleStates()) {
                models.put(BlockModelShaper.stateToModelLocation(state), block);
            }
        }
    }

    private static Map<ModelResourceLocation, ResourceLocation> cleanItemModels() {
        Map<ModelResourceLocation, ResourceLocation> models = new HashMap<ModelResourceLocation, ResourceLocation>();

        for (Block block : CONNECTED_GLASS_BLOCKS) {
            ResourceLocation id = Registry.BLOCK.getKey(block);
            models.put(inventoryModel(id), modModel("block/" + id.getPath() + "_item"));
        }

        for (Block block : CONNECTED_GLASS_PANES) {
            ResourceLocation id = Registry.BLOCK.getKey(block);
            models.put(inventoryModel(id), modModel("item/" + id.getPath() + "_item"));
        }

        return Collections.unmodifiableMap(models);
    }

    private static String glassTextureName(Block block) {
        String path = Registry.BLOCK.getKey(block).getPath();
        return path.endsWith("_pane") ? path.substring(0, path.length() - 5) : path;
    }

    private static ModelResourceLocation inventoryModel(ResourceLocation id) {
        return new ModelResourceLocation(id, "inventory");
    }

    private static ResourceLocation modModel(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @SafeVarargs
    private static <T> Set<T> immutableSet(T... values) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(values)));
    }
}
