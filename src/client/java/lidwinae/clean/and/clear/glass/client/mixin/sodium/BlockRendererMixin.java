package lidwinae.clean.and.clear.glass.client.mixin.sodium;

import lidwinae.clean.and.clear.glass.client.AccurateGlassRenderer;
import lidwinae.clean.and.clear.glass.client.SodiumCompatibleGlassModel;
import lidwinae.clean.and.clear.glass.client.SodiumQuadAdapter;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, remap = false)
    private void cleanAndClearGlass$beginBlockRender(
            BlockRenderContext context,
            ChunkBuildBuffers buffers,
            CallbackInfo callback
    ) {
        if (context.model() instanceof SodiumCompatibleGlassModel) {
            SodiumQuadAdapter.beginBlockRender();

            SodiumCompatibleGlassModel model = (SodiumCompatibleGlassModel) context.model();

            if (model.cleanAndClearGlass$requiresAccurateTransparency()) {
                AccurateGlassRenderer.capture(
                        model,
                        context.world(),
                        context.state(),
                        context.pos(),
                        context.seed()
                );
                callback.cancel();
            }
        }
    }

    @Inject(method = "getGeometry", at = @At("HEAD"), cancellable = true, remap = false)
    private void cleanAndClearGlass$getGeometry(
            BlockRenderContext context,
            Direction cullFace,
            CallbackInfoReturnable<List<BakedQuad>> callback
    ) {
        if (context.model() instanceof SodiumCompatibleGlassModel model) {
            callback.setReturnValue(SodiumQuadAdapter.getQuads(
                    model,
                    context.world(),
                    context.state(),
                    context.pos(),
                    context.seed(),
                    cullFace
            ));
        }
    }
}
