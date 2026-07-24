package lidwinae.clean.and.clear.glass.client;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

public class CleanGlassItemModel extends WrapperBakedItemModel {
    private final ItemModel cleanModel;
    private final ItemModel visibleModel;
    private final ItemModel clearModel;

    public CleanGlassItemModel(
            ItemModel wrapped,
            ItemModel cleanModel,
            ItemModel visibleModel,
            ItemModel clearModel
    ) {
        super(wrapped);
        this.cleanModel = cleanModel;
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
        if (visibleModel == null || clearModel == null) {
            return cleanModel;
        }

        return switch (config.tintedGlassStyle()) {
            case SUBTLE -> cleanModel;
            case VISIBLE -> visibleModel;
            case CLEAR -> clearModel;
        };
    }

    public record Unbaked(
            ItemModel.Unbaked wrapped,
            Identifier cleanModelId,
            Identifier visibleModelId,
            Identifier clearModelId
    ) implements ItemModel.Unbaked {
        public Unbaked(ItemModel.Unbaked wrapped, Identifier cleanModelId) {
            this(wrapped, cleanModelId, null, null);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            wrapped.resolveDependencies(resolver);
            resolver.markDependency(cleanModelId);

            if (visibleModelId != null) {
                resolver.markDependency(visibleModelId);
            }

            if (clearModelId != null) {
                resolver.markDependency(clearModelId);
            }
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ItemModel wrappedModel = wrapped.bake(context, transformation);
            ItemModel cleanModel = bakeModel(context, transformation, cleanModelId);
            ItemModel visibleModel = visibleModelId == null
                    ? null
                    : bakeModel(context, transformation, visibleModelId);
            ItemModel clearModel = clearModelId == null ? null : bakeModel(context, transformation, clearModelId);

            return new CleanGlassItemModel(wrappedModel, cleanModel, visibleModel, clearModel);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return wrapped.type();
        }

        private static ItemModel bakeModel(
                ItemModel.BakingContext context,
                Matrix4fc transformation,
                Identifier modelId
        ) {
            return new CuboidItemModelWrapper.Unbaked(
                    modelId,
                    Optional.empty(),
                    List.of()
            ).bake(context, transformation);
        }
    }
}
