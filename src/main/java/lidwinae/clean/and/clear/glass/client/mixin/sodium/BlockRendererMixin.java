package lidwinae.clean.and.clear.glass.client.mixin.sodium;

import lidwinae.clean.and.clear.glass.client.AccurateGlassRenderer;
import lidwinae.clean.and.clear.glass.client.SodiumCompatibleGlassModel;
import lidwinae.clean.and.clear.glass.client.SodiumQuadAdapter;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class BlockRendererMixin {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, remap = false)
    private void cleanAndClearGlass$beginRender(
            BlockAndTintGetter world,
            BlockState state,
            BlockPos pos,
            BakedModel bakedModel,
            ChunkModelBuffers buffers,
            boolean cull,
            long seed,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!(bakedModel instanceof SodiumCompatibleGlassModel)) {
            return;
        }

        SodiumCompatibleGlassModel model = (SodiumCompatibleGlassModel) bakedModel;
        SodiumQuadAdapter.beginBlockRender(model, world, state, pos, seed);

        if (model.cleanAndClearGlass$requiresAccurateTransparency()) {
            AccurateGlassRenderer.capture(model, world, state, pos, seed);
            SodiumQuadAdapter.endBlockRender();
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "renderModel", at = @At("RETURN"), remap = false)
    private void cleanAndClearGlass$endRender(
            BlockAndTintGetter world,
            BlockState state,
            BlockPos pos,
            BakedModel bakedModel,
            ChunkModelBuffers buffers,
            boolean cull,
            long seed,
            CallbackInfoReturnable<Boolean> callback
    ) {
        SodiumQuadAdapter.endBlockRender();
    }
}
