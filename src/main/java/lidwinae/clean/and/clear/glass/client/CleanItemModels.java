package lidwinae.clean.and.clear.glass.client;

import lidwinae.clean.and.clear.glass.CleanandClearGlass;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

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
    private static final Map<ResourceLocation, Definition> FIXED_ITEMS;

    static {
        List<Definition> all = new ArrayList<>();
        Map<ResourceLocation, Definition> fixed =
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

    static void register(ModelEvent.RegisterAdditional event) {
        for (Definition definition : ALL_DEFINITIONS) {
            event.register(definition.key());
        }
    }

    static void replace(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models =
                event.getModels();

        replaceFixed(
                models,
                minecraft("glass"),
                GLASS_CLEAR
        );

        replaceFixed(
                models,
                minecraft("glass_pane"),
                GLASS_PANE_CLEAR
        );

        replaceFixed(
                models,
                minecraft("tinted_glass"),
                selectedTintedModel()
        );

        for (Map.Entry<ResourceLocation, Definition> entry
                : FIXED_ITEMS.entrySet()) {
            replaceFixed(
                    models,
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }

    private static Definition selectedTintedModel() {
        return switch (
                CleanAndClearGlassConfig.tintedGlassStyle()
        ) {
            case SUBTLE -> TINTED_SUBTLE;
            case VISIBLE -> TINTED_VISIBLE;
            case CLEAR -> TINTED_CLEAR;
        };
    }

    private static void replaceFixed(
            Map<ModelResourceLocation, BakedModel> models,
            ResourceLocation itemId,
            Definition clean
    ) {
        ModelResourceLocation vanillaKey =
                ModelResourceLocation.inventory(itemId);

        if (!models.containsKey(vanillaKey)) {
            return;
        }

        BakedModel cleanModel = models.get(clean.key());

        if (cleanModel == null) {
            throw new IllegalStateException(
                    "Failed to bake additional model: "
                            + clean.modelId()
            );
        }

        models.put(vanillaKey, cleanModel);
    }

    private static Definition definition(String path) {
        ResourceLocation modelId =
                ResourceLocation.fromNamespaceAndPath(
                        CleanandClearGlass.MODID,
                        path
                );

        return new Definition(
                modelId,
                ModelResourceLocation.standalone(modelId)
        );
    }

    private static ResourceLocation minecraft(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private record Definition(
            ResourceLocation modelId,
            ModelResourceLocation key
    ) {
    }
}
