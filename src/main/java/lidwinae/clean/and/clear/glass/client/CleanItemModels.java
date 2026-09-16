package lidwinae.clean.and.clear.glass.client;

import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CleanItemModels {
    private static final String[] COLORS = {
            "white",
            "orange",
            "magenta",
            "light_blue",
            "yellow",
            "lime",
            "pink",
            "gray",
            "light_gray",
            "cyan",
            "purple",
            "blue",
            "brown",
            "green",
            "red",
            "black"
    };

    private static final Definition GLASS_CLEAR =
            definition("block/glass_item");

    private static final Definition GLASS_PANE_CLEAR =
            definition("item/glass_pane_item");

    private static final Definition TINTED_SUBTLE =
            definition("block/tinted_glass_item");

    private static final Definition TINTED_VISIBLE =
            definition("block/tinted_glass_visible_item");

    private static final Definition TINTED_CLEAR =
            definition("block/tinted_glass_clear_item");

    private static final List<Definition> ALL_DEFINITIONS;
    private static final Map<Identifier, Definition> FIXED_ITEMS;

    static {
        List<Definition> all = new ArrayList<>();
        Map<Identifier, Definition> fixed =
                new LinkedHashMap<>();

        all.add(GLASS_CLEAR);
        all.add(GLASS_PANE_CLEAR);
        all.add(TINTED_SUBTLE);
        all.add(TINTED_VISIBLE);
        all.add(TINTED_CLEAR);

        for (String color : COLORS) {
            String glass = color + "_stained_glass";
            String pane = glass + "_pane";

            Definition glassDefinition =
                    definition("block/" + glass + "_item");

            Definition paneDefinition =
                    definition("item/" + pane + "_item");

            all.add(glassDefinition);
            all.add(paneDefinition);

            fixed.put(minecraft(glass), glassDefinition);
            fixed.put(minecraft(pane), paneDefinition);
        }

        ALL_DEFINITIONS = List.copyOf(all);
        FIXED_ITEMS = Map.copyOf(fixed);
    }

    private CleanItemModels() {
    }

    static void register(ModelEvent.RegisterStandalone event) {
        for (Definition definition : ALL_DEFINITIONS) {
            event.register(
                    definition.key(),
                    new SimpleUnbakedStandaloneModel<>(
                            definition.modelId(),
                            CleanItemModels::bakeItemModel
                    )
            );
        }
    }

    static void replace(ModelEvent.ModifyBakingResult event) {
        Map<Identifier, ItemModel> itemModels =
                event.getBakingResult().itemStackModels();

        StandaloneModelLoader.BakedModels standalone =
                event.getBakingResult().standaloneModels();

        replaceFixed(
                itemModels,
                standalone,
                minecraft("glass"),
                GLASS_CLEAR
        );

        replaceFixed(
                itemModels,
                standalone,
                minecraft("glass_pane"),
                GLASS_PANE_CLEAR
        );

        replaceTinted(
                itemModels,
                standalone,
                minecraft("tinted_glass")
        );

        for (Map.Entry<Identifier, Definition> entry
                : FIXED_ITEMS.entrySet()) {
            ItemModel vanilla = itemModels.get(entry.getKey());

            if (vanilla != null) {
                itemModels.put(
                        entry.getKey(),
                        CleanGlassItemModel.fixed(
                                vanilla,
                                baked(
                                        standalone,
                                        entry.getValue()
                                )
                        )
                );
            }
        }
    }

    private static ItemModel bakeItemModel(
            ResolvedModel model,
            ModelBaker baker
    ) {
        TextureSlots slots = model.getTopTextureSlots();

        QuadCollection quads = model.bakeTopGeometry(
                slots,
                baker,
                BlockModelRotation.IDENTITY
        );

        ModelRenderProperties properties =
                ModelRenderProperties.fromResolvedModel(
                        baker,
                        model,
                        slots
                );

        return new CuboidItemModelWrapper(
                List.of(),
                quads,
                properties,
                new Matrix4f()
        );
    }

    private static void replaceFixed(
            Map<Identifier, ItemModel> itemModels,
            StandaloneModelLoader.BakedModels standalone,
            Identifier itemId,
            Definition clean
    ) {
        ItemModel vanilla = itemModels.get(itemId);

        if (vanilla != null) {
            itemModels.put(
                    itemId,
                    CleanGlassItemModel.fixed(
                            vanilla,
                            baked(standalone, clean)
                    )
            );
        }
    }

    private static void replaceTinted(
            Map<Identifier, ItemModel> itemModels,
            StandaloneModelLoader.BakedModels standalone,
            Identifier itemId
    ) {
        ItemModel vanilla = itemModels.get(itemId);

        if (vanilla != null) {
            itemModels.put(
                    itemId,
                    CleanGlassItemModel.tinted(
                            vanilla,
                            baked(standalone, TINTED_SUBTLE),
                            baked(standalone, TINTED_VISIBLE),
                            baked(standalone, TINTED_CLEAR)
                    )
            );
        }
    }

    private static ItemModel baked(
            StandaloneModelLoader.BakedModels models,
            Definition definition
    ) {
        ItemModel model = models.get(definition.key());

        if (model == null) {
            throw new IllegalStateException(
                    "Failed to bake standalone model: "
                            + definition.modelId()
            );
        }

        return model;
    }

    private static Definition definition(String path) {
        Identifier modelId = Identifier.fromNamespaceAndPath(
                CleanandClearGlass.MODID,
                path
        );

        StandaloneModelKey<ItemModel> key =
                new StandaloneModelKey<>(
                        () -> "Clean and Clear Glass: " + modelId
                );

        return new Definition(modelId, key);
    }

    private static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath(
                "minecraft",
                path
        );
    }

    private record Definition(
            Identifier modelId,
            StandaloneModelKey<ItemModel> key
    ) {
    }
}
