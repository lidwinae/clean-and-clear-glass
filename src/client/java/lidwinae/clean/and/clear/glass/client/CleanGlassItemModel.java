package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class CleanGlassItemModel extends ForwardingBakedModel {
    private final BakedModel subtleModel;
    private final BakedModel visibleModel;
    private final BakedModel clearModel;

    public CleanGlassItemModel(
            BakedModel wrapped,
            BakedModel subtleModel,
            BakedModel visibleModel,
            BakedModel clearModel
    ) {
        super(wrapped);
        this.subtleModel = subtleModel;
        this.visibleModel = visibleModel;
        this.clearModel = clearModel;
    }

    public CleanGlassItemModel(BakedModel wrapped, BakedModel cleanModel) {
        this(wrapped, cleanModel, cleanModel, cleanModel);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(
            ItemStack stack,
            Supplier<RandomSource> randomSupplier,
            RenderContext context
    ) {
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        BakedModel selectedModel = wrapped;

        if (config.enabled()) {
            selectedModel = switch (config.tintedGlassStyle()) {
                case SUBTLE -> subtleModel;
                case VISIBLE -> visibleModel;
                case CLEAR -> clearModel;
            };
        }

        selectedModel.emitItemQuads(stack, randomSupplier, context);
    }
}
