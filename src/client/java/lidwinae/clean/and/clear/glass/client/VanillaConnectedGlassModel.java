package lidwinae.clean.and.clear.glass.client;

import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaConnectedGlassModel extends WrapperBlockStateModel {
    private static final float T = 1.0F / 16.0F;
    private static final float TINTED_CENTER_DEPTH = 1.0F / 1024.0F;
    private static final float CENTER_UV_MIN = 2.0F / 16.0F;
    private static final float CENTER_UV_MAX = 14.0F / 16.0F;

    private final Material.Baked tintedGlassCenterMaterial;

    public VanillaConnectedGlassModel(BlockStateModel wrapped) {
        this(wrapped, null);
    }

    public VanillaConnectedGlassModel(BlockStateModel wrapped, Material.Baked tintedGlassCenterMaterial) {
        super(wrapped);
        this.tintedGlassCenterMaterial = tintedGlassCenterMaterial;
    }

    @Override
    public void emitQuads(
            QuadEmitter emitter,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            Predicate<Direction> cullTest
    ) {
        if (level == BlockAndTintGetter.EMPTY) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        Material.Baked edgeMaterial = wrapped.particleMaterial();
        boolean useFullCenterTexture = state.getBlock() == Blocks.TINTED_GLASS && tintedGlassCenterMaterial != null;
        Material.Baked centerMaterial = useFullCenterTexture ? tintedGlassCenterMaterial : edgeMaterial;

        boolean up = connectsToGlass(level, pos.above(), state);
        boolean down = connectsToGlass(level, pos.below(), state);
        boolean north = connectsToGlass(level, pos.north(), state);
        boolean east = connectsToGlass(level, pos.east(), state);
        boolean south = connectsToGlass(level, pos.south(), state);
        boolean west = connectsToGlass(level, pos.west(), state);

        // Render the clear center only on faces that are exposed to non-matching blocks.
        if (!north) emitCenter(emitter, centerMaterial, Direction.NORTH, east, down, west, up, useFullCenterTexture);
        if (!east) emitCenter(emitter, centerMaterial, Direction.EAST, south, down, north, up, useFullCenterTexture);
        if (!south) emitCenter(emitter, centerMaterial, Direction.SOUTH, west, down, east, up, useFullCenterTexture);
        if (!west) emitCenter(emitter, centerMaterial, Direction.WEST, north, down, south, up, useFullCenterTexture);
        if (!up) emitCenter(emitter, centerMaterial, Direction.UP, west, south, east, north, useFullCenterTexture);
        if (!down) emitCenter(emitter, centerMaterial, Direction.DOWN, west, north, east, south, useFullCenterTexture);

        // Add one-pixel border strips only where the neighboring same glass block is absent.
        if (!north && !up) emitStrip(emitter, edgeMaterial, Direction.NORTH, 0, 1 - T, 1, 1, 0);
        if (!north && !down) emitStrip(emitter, edgeMaterial, Direction.NORTH, 0, 0, 1, T, 0);
        if (!south && !up) emitStrip(emitter, edgeMaterial, Direction.SOUTH, 0, 1 - T, 1, 1, 0);
        if (!south && !down) emitStrip(emitter, edgeMaterial, Direction.SOUTH, 0, 0, 1, T, 0);

        if (!east && !up) emitStrip(emitter, edgeMaterial, Direction.EAST, 0, 1 - T, 1, 1, 0);
        if (!east && !down) emitStrip(emitter, edgeMaterial, Direction.EAST, 0, 0, 1, T, 0);
        if (!west && !up) emitStrip(emitter, edgeMaterial, Direction.WEST, 0, 1 - T, 1, 1, 0);
        if (!west && !down) emitStrip(emitter, edgeMaterial, Direction.WEST, 0, 0, 1, T, 0);

        if (!north && !east) emitStrip(emitter, edgeMaterial, Direction.NORTH, 0, 0, T, 1, 0);
        if (!north && !west) emitStrip(emitter, edgeMaterial, Direction.NORTH, 1 - T, 0, 1, 1, 0);
        if (!south && !east) emitStrip(emitter, edgeMaterial, Direction.SOUTH, 1 - T, 0, 1, 1, 0);
        if (!south && !west) emitStrip(emitter, edgeMaterial, Direction.SOUTH, 0, 0, T, 1, 0);

        if (!east && !north) emitStrip(emitter, edgeMaterial, Direction.EAST, 1 - T, 0, 1, 1, 0);
        if (!east && !south) emitStrip(emitter, edgeMaterial, Direction.EAST, 0, 0, T, 1, 0);
        if (!west && !north) emitStrip(emitter, edgeMaterial, Direction.WEST, 0, 0, T, 1, 0);
        if (!west && !south) emitStrip(emitter, edgeMaterial, Direction.WEST, 1 - T, 0, 1, 1, 0);

        if (!up && !north) emitStrip(emitter, edgeMaterial, Direction.UP, 0, 1 - T, 1, 1, 0);
        if (!up && !south) emitStrip(emitter, edgeMaterial, Direction.UP, 0, 0, 1, T, 0);
        if (!up && !east) emitStrip(emitter, edgeMaterial, Direction.UP, 1 - T, 0, 1, 1, 0);
        if (!up && !west) emitStrip(emitter, edgeMaterial, Direction.UP, 0, 0, T, 1, 0);

        if (!down && !north) emitStrip(emitter, edgeMaterial, Direction.DOWN, 0, 0, 1, T, 0);
        if (!down && !south) emitStrip(emitter, edgeMaterial, Direction.DOWN, 0, 1 - T, 1, 1, 0);
        if (!down && !east) emitStrip(emitter, edgeMaterial, Direction.DOWN, 1 - T, 0, 1, 1, 0);
        if (!down && !west) emitStrip(emitter, edgeMaterial, Direction.DOWN, 0, 0, T, 1, 0);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (level == BlockAndTintGetter.EMPTY) {
            return VanillaConnectedGlassModel.class;
        }

        return new ConnectionKey(
                connectsToGlass(level, pos.above(), state),
                connectsToGlass(level, pos.below(), state),
                connectsToGlass(level, pos.north(), state),
                connectsToGlass(level, pos.east(), state),
                connectsToGlass(level, pos.south(), state),
                connectsToGlass(level, pos.west(), state)
        );
    }

    private static boolean connectsToGlass(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos).getBlock() == state.getBlock();
    }

    private static void emitStrip(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        emitter.square(face, left, bottom, right, top, depth)
                .materialBake(material, MutableQuadView.BAKE_LOCK_UV)
                .diffuseShade(false)
                .emit();
    }

    private static void emitCenter(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            boolean connectLeft,
            boolean connectBottom,
            boolean connectRight,
            boolean connectTop,
            boolean fullTexture
    ) {
        float left = fullTexture || connectLeft ? 0 : T;
        float bottom = fullTexture || connectBottom ? 0 : T;
        float right = fullTexture || connectRight ? 1 : 1 - T;
        float top = fullTexture || connectTop ? 1 : 1 - T;
        float uvLeft = fullTexture ? 0 : CENTER_UV_MIN;
        float uvBottom = fullTexture ? 0 : CENTER_UV_MIN;
        float uvRight = fullTexture ? 1 : CENTER_UV_MAX;
        float uvTop = fullTexture ? 1 : CENTER_UV_MAX;
        float depth = fullTexture ? TINTED_CENTER_DEPTH : 0;

        emitter.square(face, left, bottom, right, top, depth)
                .uv(0, uvLeft, uvBottom)
                .uv(1, uvLeft, uvTop)
                .uv(2, uvRight, uvTop)
                .uv(3, uvRight, uvBottom)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private record ConnectionKey(boolean up, boolean down, boolean north, boolean east, boolean south, boolean west) {
    }
}
