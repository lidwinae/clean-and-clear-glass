package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Random;

public final class CleanGlassItemModel extends ForwardingBakedModel {
    private final BakedModel cleanModel;

    public CleanGlassItemModel(BakedModel wrapped, BakedModel cleanModel) {
        this.wrapped = wrapped;
        this.cleanModel = cleanModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random) {
        BakedModel selected = CleanAndClearGlassConfig.get().enabled() ? cleanModel : wrapped;
        return selected.getQuads(state, face, random);
    }
}
