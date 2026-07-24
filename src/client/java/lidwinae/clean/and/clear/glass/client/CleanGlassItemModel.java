package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

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
        this.wrapped = wrapped;
        this.subtleModel = subtleModel;
        this.visibleModel = visibleModel;
        this.clearModel = clearModel;
    }

    public CleanGlassItemModel(BakedModel wrapped, BakedModel cleanModel) {
        this(wrapped, cleanModel, cleanModel, cleanModel);
    }

    @Override
    public boolean isVanillaAdapter() {
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random) {
        return selectedModel().getQuads(state, face, random);
    }

    private BakedModel selectedModel() {
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        BakedModel selectedModel = wrapped;

        if (config.enabled()) {
            selectedModel = switch (config.tintedGlassStyle()) {
                case SUBTLE -> subtleModel;
                case VISIBLE -> visibleModel;
                case CLEAR -> clearModel;
            };
        }

        return selectedModel;
    }
}
