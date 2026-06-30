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
import net.minecraft.world.level.block.state.BlockState;

public class VanillaConnectedGlassModel extends WrapperBlockStateModel {
    private static final float T = 1.0F / 16.0F;
    private static final float CENTER_UV_MIN = 2.0F / 16.0F;
    private static final float CENTER_UV_MAX = 14.0F / 16.0F;

    public VanillaConnectedGlassModel(BlockStateModel wrapped) {
        super(wrapped);
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

        Material.Baked material = wrapped.particleMaterial();

        boolean up = connectsToGlass(level, pos.above(), state);
        boolean down = connectsToGlass(level, pos.below(), state);
        boolean north = connectsToGlass(level, pos.north(), state);
        boolean east = connectsToGlass(level, pos.east(), state);
        boolean south = connectsToGlass(level, pos.south(), state);
        boolean west = connectsToGlass(level, pos.west(), state);

        // Render the clear center only on faces that are exposed to non-matching blocks.
        if (!north) emitCenter(emitter, material, Direction.NORTH, east, down, west, up);
        if (!east) emitCenter(emitter, material, Direction.EAST, south, down, north, up);
        if (!south) emitCenter(emitter, material, Direction.SOUTH, west, down, east, up);
        if (!west) emitCenter(emitter, material, Direction.WEST, north, down, south, up);
        if (!up) emitCenter(emitter, material, Direction.UP, west, south, east, north);
        if (!down) emitCenter(emitter, material, Direction.DOWN, west, north, east, south);

        // Add one-pixel border strips only where the neighboring same glass block is absent.
        if (!north && !up) emitStrip(emitter, material, Direction.NORTH, 0, 1 - T, 1, 1, 0);
        if (!north && !down) emitStrip(emitter, material, Direction.NORTH, 0, 0, 1, T, 0);
        if (!south && !up) emitStrip(emitter, material, Direction.SOUTH, 0, 1 - T, 1, 1, 0);
        if (!south && !down) emitStrip(emitter, material, Direction.SOUTH, 0, 0, 1, T, 0);

        if (!east && !up) emitStrip(emitter, material, Direction.EAST, 0, 1 - T, 1, 1, 0);
        if (!east && !down) emitStrip(emitter, material, Direction.EAST, 0, 0, 1, T, 0);
        if (!west && !up) emitStrip(emitter, material, Direction.WEST, 0, 1 - T, 1, 1, 0);
        if (!west && !down) emitStrip(emitter, material, Direction.WEST, 0, 0, 1, T, 0);

        if (!north && !east) emitStrip(emitter, material, Direction.NORTH, 0, 0, T, 1, 0);
        if (!north && !west) emitStrip(emitter, material, Direction.NORTH, 1 - T, 0, 1, 1, 0);
        if (!south && !east) emitStrip(emitter, material, Direction.SOUTH, 1 - T, 0, 1, 1, 0);
        if (!south && !west) emitStrip(emitter, material, Direction.SOUTH, 0, 0, T, 1, 0);

        if (!east && !north) emitStrip(emitter, material, Direction.EAST, 1 - T, 0, 1, 1, 0);
        if (!east && !south) emitStrip(emitter, material, Direction.EAST, 0, 0, T, 1, 0);
        if (!west && !north) emitStrip(emitter, material, Direction.WEST, 0, 0, T, 1, 0);
        if (!west && !south) emitStrip(emitter, material, Direction.WEST, 1 - T, 0, 1, 1, 0);

        if (!up && !north) emitStrip(emitter, material, Direction.UP, 0, 1 - T, 1, 1, 0);
        if (!up && !south) emitStrip(emitter, material, Direction.UP, 0, 0, 1, T, 0);
        if (!up && !east) emitStrip(emitter, material, Direction.UP, 1 - T, 0, 1, 1, 0);
        if (!up && !west) emitStrip(emitter, material, Direction.UP, 0, 0, T, 1, 0);

        if (!down && !north) emitStrip(emitter, material, Direction.DOWN, 0, 0, 1, T, 0);
        if (!down && !south) emitStrip(emitter, material, Direction.DOWN, 0, 1 - T, 1, 1, 0);
        if (!down && !east) emitStrip(emitter, material, Direction.DOWN, 1 - T, 0, 1, 1, 0);
        if (!down && !west) emitStrip(emitter, material, Direction.DOWN, 0, 0, T, 1, 0);
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
            boolean connectTop
    ) {
        float left = connectLeft ? 0 : T;
        float bottom = connectBottom ? 0 : T;
        float right = connectRight ? 1 : 1 - T;
        float top = connectTop ? 1 : 1 - T;

        emitter.square(face, left, bottom, right, top, 0)
                .uv(0, CENTER_UV_MIN, CENTER_UV_MIN)
                .uv(1, CENTER_UV_MIN, CENTER_UV_MAX)
                .uv(2, CENTER_UV_MAX, CENTER_UV_MAX)
                .uv(3, CENTER_UV_MAX, CENTER_UV_MIN)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private record ConnectionKey(boolean up, boolean down, boolean north, boolean east, boolean south, boolean west) {
    }
}
