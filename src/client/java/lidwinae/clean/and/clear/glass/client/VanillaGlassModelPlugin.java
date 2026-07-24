package lidwinae.clean.and.clear.glass.client;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class VanillaGlassModelPlugin {
    private VanillaGlassModelPlugin() {}

    private static final String MOD_ID = "clean-and-clear-glass";
    private static final Identifier TINTED_GLASS_ITEM_ID = Identifier.fromNamespaceAndPath("minecraft", "tinted_glass");
    private static final Identifier SUBTLE_TINTED_GLASS_ITEM_MODEL_ID = Identifier.fromNamespaceAndPath(
            MOD_ID,
            "block/tinted_glass_item"
    );
    private static final Identifier VISIBLE_TINTED_GLASS_ITEM_MODEL_ID = Identifier.fromNamespaceAndPath(
            MOD_ID,
            "block/tinted_glass_visible_item"
    );
    private static final Identifier CLEAR_TINTED_GLASS_ITEM_MODEL_ID = Identifier.fromNamespaceAndPath(
            MOD_ID,
            "block/tinted_glass_clear_item"
    );

    // Full glass blocks keep their vanilla block IDs, but use connected client geometry.
    private static final Set<Block> CONNECTED_GLASS_BLOCKS = connectedGlassBlocks();

    // Pane variants need their own renderer because pane geometry changes by direction.
    private static final Set<Block> CONNECTED_GLASS_PANES = connectedGlassPanes();
    private static final Set<Identifier> CLEAN_GLASS_BLOCK_ITEM_IDS = CONNECTED_GLASS_BLOCKS.stream()
            .filter(block -> block != Blocks.TINTED_GLASS)
            .map(BuiltInRegistries.BLOCK::getKey)
            .collect(Collectors.toUnmodifiableSet());
    private static final Set<Identifier> CLEAN_GLASS_PANE_ITEM_IDS = blockIdsFor(CONNECTED_GLASS_PANES);

    public static void register() {
        ModelLoadingPlugin.register((pluginContext) -> {
            // Wrap the baked vanilla model after all normal model loading has completed.
            pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
                CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();

                if (!config.enabled()) {
                    return model;
                }

                Block block = context.state().getBlock();

                if (CONNECTED_GLASS_BLOCKS.contains(block)) {
                    if (block == Blocks.TINTED_GLASS) {
                        CleanAndClearGlassConfig.TintedGlassStyle style = config.tintedGlassStyle();
                        Material.Baked edgeMaterial = context.baker().materials().get(
                                textureMaterial(style.edgeTexture()),
                                () -> "clean-and-clear-glass tinted glass edge"
                        );
                        Material.Baked centerMaterial = context.baker().materials().get(
                                textureMaterial(style.centerTexture()),
                                () -> "clean-and-clear-glass tinted glass center"
                        );

                        return new VanillaConnectedGlassModel(model, edgeMaterial, centerMaterial, true);
                    }

                    if (block == Blocks.GLASS) {
                        Material.Baked edgeMaterial = context.baker().materials().get(
                                textureMaterial("glass"),
                                () -> "clean-and-clear-glass regular glass edge"
                        );
                        String centerTexture = config.glassStyle().centerTexture();

                        if (centerTexture != null) {
                            Material.Baked centerMaterial = context.baker().materials().get(
                                    textureMaterial(centerTexture),
                                    () -> "clean-and-clear-glass regular glass center"
                            );

                            return new VanillaConnectedGlassModel(model, edgeMaterial, centerMaterial, true);
                        }

                        return new VanillaConnectedGlassModel(model, edgeMaterial);
                    }

                    String textureName = glassTextureName(block);
                    Material.Baked material = context.baker().materials().get(
                            textureMaterial(textureName),
                            () -> "clean-and-clear-glass " + textureName
                    );

                    return new VanillaConnectedGlassModel(model, material);
                }

                if (CONNECTED_GLASS_PANES.contains(block)) {
                    if (block == Blocks.GLASS_PANE) {
                        Material.Baked edgeMaterial = context.baker().materials().get(
                                textureMaterial("glass"),
                                () -> "clean-and-clear-glass regular glass pane edge"
                        );
                        String centerTexture = config.glassStyle().centerTexture();

                        if (centerTexture != null) {
                            Material.Baked centerMaterial = context.baker().materials().get(
                                    textureMaterial(centerTexture),
                                    () -> "clean-and-clear-glass regular glass pane center"
                            );

                            return new VanillaConnectedGlassPaneModel(model, edgeMaterial, centerMaterial);
                        }

                        return new VanillaConnectedGlassPaneModel(model, edgeMaterial);
                    }

                    String textureName = glassTextureName(block);
                    Material.Baked material = context.baker().materials().get(
                            textureMaterial(textureName),
                            () -> "clean-and-clear-glass " + textureName
                    );

                    return new VanillaConnectedGlassPaneModel(model, material);
                }

                return model;
            });

            pluginContext.modifyItemModelBeforeBake().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
                if (TINTED_GLASS_ITEM_ID.equals(context.itemId())) {
                    return new CleanGlassItemModel.Unbaked(
                            model,
                            SUBTLE_TINTED_GLASS_ITEM_MODEL_ID,
                            VISIBLE_TINTED_GLASS_ITEM_MODEL_ID,
                            CLEAR_TINTED_GLASS_ITEM_MODEL_ID
                    );
                }

                Identifier cleanModelId = cleanItemModelId(context.itemId());

                if (cleanModelId != null) {
                    return new CleanGlassItemModel.Unbaked(model, cleanModelId);
                }

                return model;
            });
        });
    }

    private static Material textureMaterial(String textureName) {
        return new Material(Identifier.fromNamespaceAndPath(MOD_ID, "block/" + textureName), true);
    }

    private static Identifier modModel(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static Identifier cleanItemModelId(Identifier itemId) {
        String path = itemId.getPath();

        if (CLEAN_GLASS_BLOCK_ITEM_IDS.contains(itemId)) {
            return modModel("block/" + path + "_item");
        }

        if (CLEAN_GLASS_PANE_ITEM_IDS.contains(itemId)) {
            return modModel("item/" + path + "_item");
        }

        return null;
    }

    private static Set<Identifier> blockIdsFor(Set<Block> blocks) {
        return blocks.stream()
                .map(BuiltInRegistries.BLOCK::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<Block> connectedGlassBlocks() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.GLASS);
        blocks.add(Blocks.TINTED_GLASS);
        blocks.addAll(Blocks.STAINED_GLASS.asList());
        return Set.copyOf(blocks);
    }

    private static Set<Block> connectedGlassPanes() {
        Set<Block> blocks = new HashSet<>();
        blocks.add(Blocks.GLASS_PANE);
        blocks.addAll(Blocks.STAINED_GLASS_PANE.asList());
        return Set.copyOf(blocks);
    }

    private static String glassTextureName(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return path.endsWith("_pane") ? path.substring(0, path.length() - "_pane".length()) : path;
    }
}
