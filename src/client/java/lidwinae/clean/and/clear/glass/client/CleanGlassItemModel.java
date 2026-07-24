package lidwinae.clean.and.clear.glass.client;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CleanGlassItemModel extends WrapperBakedItemModel {
    private final ItemModel subtleModel;
    private final ItemModel visibleModel;
    private final ItemModel clearModel;

    public CleanGlassItemModel(
            ItemModel wrapped,
            ItemModel subtleModel,
            ItemModel visibleModel,
            ItemModel clearModel
    ) {
        super(wrapped);
        this.subtleModel = subtleModel;
        this.visibleModel = visibleModel;
        this.clearModel = clearModel;
    }

    @Override
    public void update(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            ClientLevel level,
            ItemOwner owner,
            int seed
    ) {
        CleanAndClearGlassConfig config = CleanAndClearGlassConfig.get();
        ItemModel selectedModel = wrapped;

        if (config.enabled()) {
            selectedModel = selectedCleanModel(config);
        }

        selectedModel.update(renderState, stack, resolver, displayContext, level, owner, seed);
    }

    private ItemModel selectedCleanModel(CleanAndClearGlassConfig config) {
        return switch (config.tintedGlassStyle()) {
            case SUBTLE -> subtleModel;
            case VISIBLE -> visibleModel;
            case CLEAR -> clearModel;
        };
    }

    public record Unbaked(
            ItemModel.Unbaked wrapped,
            Identifier subtleModelId,
            Identifier visibleModelId,
            Identifier clearModelId
    ) implements ItemModel.Unbaked {
        public Unbaked(ItemModel.Unbaked wrapped, Identifier cleanModelId) {
            this(wrapped, cleanModelId, cleanModelId, cleanModelId);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            wrapped.resolveDependencies(resolver);
            resolver.markDependency(subtleModelId);
            resolver.markDependency(visibleModelId);
            resolver.markDependency(clearModelId);
        }

        @Override
        public ItemModel bake(BakingContext context) {
            ItemModel wrappedModel = wrapped.bake(context);
            ItemModel subtleModel = bakeModel(context, subtleModelId);
            ItemModel visibleModel = bakeModel(context, visibleModelId);
            ItemModel clearModel = bakeModel(context, clearModelId);

            return new CleanGlassItemModel(wrappedModel, subtleModel, visibleModel, clearModel);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return wrapped.type();
        }

        private static ItemModel bakeModel(
                BakingContext context,
                Identifier modelId
        ) {
            return new BlockModelWrapper.Unbaked(
                    modelId,
                    List.of()
            ).bake(context);
        }
    }
}
