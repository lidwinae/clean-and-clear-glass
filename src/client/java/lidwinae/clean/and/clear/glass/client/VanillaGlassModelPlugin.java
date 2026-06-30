package lidwinae.clean.and.clear.glass.client;

import java.util.Set;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class VanillaGlassModelPlugin {
    private VanillaGlassModelPlugin() {}

    private static final Material TINTED_GLASS_CENTER_MATERIAL = new Material(
            Identifier.fromNamespaceAndPath("minecraft", "block/tinted_glass_center"),
            true
    );

    // Full glass blocks keep their vanilla block IDs, but use connected client geometry.
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

    // Pane variants need their own renderer because pane geometry changes by direction.
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

    public static void register() {
        ModelLoadingPlugin.register((pluginContext) -> {
            // Wrap the baked vanilla model after all normal model loading has completed.
            pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
                Block block = context.state().getBlock();

                if (CONNECTED_GLASS_BLOCKS.contains(block)) {
                    if (block == Blocks.TINTED_GLASS) {
                        Material.Baked centerMaterial = context.baker().materials().get(
                                TINTED_GLASS_CENTER_MATERIAL,
                                () -> "clean-and-clear-glass tinted glass center"
                        );

                        return new VanillaConnectedGlassModel(model, centerMaterial);
                    }

                    return new VanillaConnectedGlassModel(model);
                }

                if (CONNECTED_GLASS_PANES.contains(block)) {
                    return new VanillaConnectedGlassPaneModel(model);
                }

                return model;
            });
        });
    }
}
