package lidwinae.clean.and.clear.glass.client.mixin;

import lidwinae.clean.and.clear.glass.client.AccurateGlassRenderer;
import lidwinae.clean.and.clear.glass.client.VanillaGlassModelPlugin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Inject(
            method = "apply(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("RETURN")
    )
    private void cleanAndClearGlass$wrapModels(
            ModelBakery bakery,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo callback
    ) {
        ModelManager manager = (ModelManager) (Object) this;
        bakedRegistry = VanillaGlassModelPlugin.wrapModels(manager, bakedRegistry);
        manager.getBlockModelShaper().rebuildCache();
        AccurateGlassRenderer.clear();
    }
}
