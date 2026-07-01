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
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VanillaConnectedGlassPaneModel extends WrapperBlockStateModel {
    // Vanilla pane thickness is two pixels wide, centered at 8/16.
    private static final float PANE_MIN = 7.0F / 16.0F;
    private static final float PANE_MAX = 9.0F / 16.0F;
    private static final float EPSILON = 0.0001F;

    // Pull endpoint caps slightly inward so they remain visible against adjacent blocks.
    private static final float CAP_INSET = 1.0F / 1024.0F;

    // One texture pixel from each side of the 16x16 glass texture.
    private static final float EDGE_STRIP = 1.0F / 16.0F;
    private static final float EDGE_START = 0.0F;
    private static final float EDGE_END = EDGE_STRIP;
    private static final float OPPOSITE_EDGE_START = 1.0F - EDGE_STRIP;
    private static final float OPPOSITE_EDGE_END = 1.0F;

    public VanillaConnectedGlassPaneModel(BlockStateModel wrapped) {
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

        boolean north = state.getValue(CrossCollisionBlock.NORTH);
        boolean east = state.getValue(CrossCollisionBlock.EAST);
        boolean south = state.getValue(CrossCollisionBlock.SOUTH);
        boolean west = state.getValue(CrossCollisionBlock.WEST);

        boolean sameNorth = connectsToSamePane(level, pos.north(), state);
        boolean sameEast = connectsToSamePane(level, pos.east(), state);
        boolean sameSouth = connectsToSamePane(level, pos.south(), state);
        boolean sameWest = connectsToSamePane(level, pos.west(), state);

        boolean centerUp = hasSamePaneSegment(level, pos.above(), state, null);
        boolean centerDown = hasSamePaneSegment(level, pos.below(), state, null);

        boolean northUp = hasSamePaneSegment(level, pos.above(), state, Direction.NORTH);
        boolean northDown = hasSamePaneSegment(level, pos.below(), state, Direction.NORTH);

        boolean eastUp = hasSamePaneSegment(level, pos.above(), state, Direction.EAST);
        boolean eastDown = hasSamePaneSegment(level, pos.below(), state, Direction.EAST);

        boolean southUp = hasSamePaneSegment(level, pos.above(), state, Direction.SOUTH);
        boolean southDown = hasSamePaneSegment(level, pos.below(), state, Direction.SOUTH);

        boolean westUp = hasSamePaneSegment(level, pos.above(), state, Direction.WEST);
        boolean westDown = hasSamePaneSegment(level, pos.below(), state, Direction.WEST);

        int connectedSides = (north ? 1 : 0)
                + (east ? 1 : 0)
                + (south ? 1 : 0)
                + (west ? 1 : 0);

        boolean crossIntersection = connectedSides == 4;
        boolean tIntersection = connectedSides == 3;

        // Cross intersections fill all center faces.
        // T intersections add one extra center face opposite the missing side.
        boolean showCenterNorth = crossIntersection || !north || (tIntersection && !south);
        boolean showCenterEast = crossIntersection || !east || (tIntersection && !west);
        boolean showCenterSouth = crossIntersection || !south || (tIntersection && !north);
        boolean showCenterWest = crossIntersection || !west || (tIntersection && !east);

        emitBox(emitter, material, PANE_MIN, 0, PANE_MIN, PANE_MAX, 1, PANE_MAX,
                showCenterNorth,
                showCenterEast,
                showCenterSouth,
                showCenterWest,
                true,
                true,
                sameNorth, sameEast, sameSouth, sameWest,
                centerUp, centerDown);

        if (north) {
            emitBox(emitter, material, PANE_MIN, 0, 0, PANE_MAX, 1, PANE_MIN,
                    !sameNorth, true, false, true, true, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    northUp, northDown);
        }

        if (east) {
            emitBox(emitter, material, PANE_MAX, 0, PANE_MIN, 1, 1, PANE_MAX,
                    true, !sameEast, true, false, true, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    eastUp, eastDown);
        }

        if (south) {
            emitBox(emitter, material, PANE_MIN, 0, PANE_MAX, PANE_MAX, 1, 1,
                    false, true, !sameSouth, true, true, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    southUp, southDown);
        }

        if (west) {
            emitBox(emitter, material, 0, 0, PANE_MIN, PANE_MIN, 1, PANE_MAX,
                    true, false, true, !sameWest, true, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    westUp, westDown);
        }
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        if (level == BlockAndTintGetter.EMPTY) {
            return VanillaConnectedGlassPaneModel.class;
        }

        return new PaneConnectionKey(
                state.getValue(CrossCollisionBlock.NORTH),
                state.getValue(CrossCollisionBlock.EAST),
                state.getValue(CrossCollisionBlock.SOUTH),
                state.getValue(CrossCollisionBlock.WEST),
                connectsToSamePane(level, pos.north(), state),
                connectsToSamePane(level, pos.east(), state),
                connectsToSamePane(level, pos.south(), state),
                connectsToSamePane(level, pos.west(), state),
                hasSamePaneSegment(level, pos.above(), state, null),
                hasSamePaneSegment(level, pos.below(), state, null),
                hasSamePaneSegment(level, pos.above(), state, Direction.NORTH),
                hasSamePaneSegment(level, pos.below(), state, Direction.NORTH),
                hasSamePaneSegment(level, pos.above(), state, Direction.EAST),
                hasSamePaneSegment(level, pos.below(), state, Direction.EAST),
                hasSamePaneSegment(level, pos.above(), state, Direction.SOUTH),
                hasSamePaneSegment(level, pos.below(), state, Direction.SOUTH),
                hasSamePaneSegment(level, pos.above(), state, Direction.WEST),
                hasSamePaneSegment(level, pos.below(), state, Direction.WEST)
        );
    }

    private static boolean connectsToSamePane(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos).getBlock() == state.getBlock();
    }

    private static boolean hasSamePaneSegment(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction segment) {
        BlockState otherState = level.getBlockState(pos);

        if (otherState.getBlock() != state.getBlock()) {
            return false;
        }

        return segment == null || connectsInDirection(otherState, segment);
    }

    private static boolean connectsInDirection(BlockState state, Direction direction) {
        return switch (direction) {
            case NORTH -> state.getValue(CrossCollisionBlock.NORTH);
            case EAST -> state.getValue(CrossCollisionBlock.EAST);
            case SOUTH -> state.getValue(CrossCollisionBlock.SOUTH);
            case WEST -> state.getValue(CrossCollisionBlock.WEST);
            default -> true;
        };
    }

    private static void emitBox(
            QuadEmitter emitter,
            Material.Baked material,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            boolean showNorth,
            boolean showEast,
            boolean showSouth,
            boolean showWest,
            boolean showUp,
            boolean showDown,
            boolean sameNorth,
            boolean sameEast,
            boolean sameSouth,
            boolean sameWest,
            boolean sameUp,
            boolean sameDown
    ) {
        if (showNorth) emitFace(emitter, material, Direction.NORTH, minX, minY, minZ, maxX, maxY, maxZ, sameNorth, sameEast, sameSouth, sameWest, sameUp, sameDown);
        if (showEast) emitFace(emitter, material, Direction.EAST, minX, minY, minZ, maxX, maxY, maxZ, sameNorth, sameEast, sameSouth, sameWest, sameUp, sameDown);
        if (showSouth) emitFace(emitter, material, Direction.SOUTH, minX, minY, minZ, maxX, maxY, maxZ, sameNorth, sameEast, sameSouth, sameWest, sameUp, sameDown);
        if (showWest) emitFace(emitter, material, Direction.WEST, minX, minY, minZ, maxX, maxY, maxZ, sameNorth, sameEast, sameSouth, sameWest, sameUp, sameDown);

        if (showUp && !sameUp) {
            emitTopBottomFace(emitter, material, Direction.UP, minX, minY, minZ, maxX, maxY, maxZ);
        }

        if (showDown && !sameDown) {
            emitTopBottomFace(emitter, material, Direction.DOWN, minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static void emitFace(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            boolean sameNorth,
            boolean sameEast,
            boolean sameSouth,
            boolean sameWest,
            boolean sameUp,
            boolean sameDown
    ) {
        if (isOuterCap(face, minX, minZ, maxX, maxZ)) {
            emitSplitEdgeCapFace(emitter, material, face, minX, minY, minZ, maxX, maxY, maxZ);
            return;
        }
        switch (face) {
            case NORTH -> {
                emitter.pos(0, maxX, maxY, minZ);
                emitter.pos(1, maxX, minY, minZ);
                emitter.pos(2, minX, minY, minZ);
                emitter.pos(3, minX, maxY, minZ);
            }
            case SOUTH -> {
                emitter.pos(0, minX, maxY, maxZ);
                emitter.pos(1, minX, minY, maxZ);
                emitter.pos(2, maxX, minY, maxZ);
                emitter.pos(3, maxX, maxY, maxZ);
            }
            case WEST -> {
                emitter.pos(0, minX, maxY, minZ);
                emitter.pos(1, minX, minY, minZ);
                emitter.pos(2, minX, minY, maxZ);
                emitter.pos(3, minX, maxY, maxZ);
            }
            case EAST -> {
                emitter.pos(0, maxX, maxY, maxZ);
                emitter.pos(1, maxX, minY, maxZ);
                emitter.pos(2, maxX, minY, minZ);
                emitter.pos(3, maxX, maxY, minZ);
            }
            default -> {
                return;
            }
        }

        applySideUv(emitter, face, minX, minZ, maxX, maxZ, sameNorth, sameEast, sameSouth, sameWest, sameUp, sameDown);

        emitter.nominalFace(face)
                .cullFace(null)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private static void emitTopBottomFace(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
        float lengthX = maxX - minX;
        float lengthZ = maxZ - minZ;

        if (lengthX >= lengthZ) {
            // East-west panes use the top and bottom texture rows: Y1 and Y16.
            float midZ = (minZ + maxZ) * 0.5F;

            emitTopBottomQuad(emitter, material, face,
                    minX, minY, minZ,
                    maxX, maxY, midZ,
                    minX, maxX,
                    EDGE_START, EDGE_END);

            emitTopBottomQuad(emitter, material, face,
                    minX, minY, midZ,
                    maxX, maxY, maxZ,
                    minX, maxX,
                    OPPOSITE_EDGE_START, OPPOSITE_EDGE_END);
        } else {
            // North-south panes use the left and right texture columns: X1 and X16.
            float midX = (minX + maxX) * 0.5F;

            emitTopBottomQuad(emitter, material, face,
                    minX, minY, minZ,
                    midX, maxY, maxZ,
                    EDGE_START, EDGE_END,
                    minZ, maxZ);

            emitTopBottomQuad(emitter, material, face,
                    midX, minY, minZ,
                    maxX, maxY, maxZ,
                    OPPOSITE_EDGE_START, OPPOSITE_EDGE_END,
                    minZ, maxZ);
        }
    }

    private static void emitTopBottomQuad(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1,
            float v0,
            float v1
    ) {
        if (face == Direction.UP) {
            emitter.pos(0, minX, maxY, minZ);
            emitter.pos(1, minX, maxY, maxZ);
            emitter.pos(2, maxX, maxY, maxZ);
            emitter.pos(3, maxX, maxY, minZ);
        } else {
            emitter.pos(0, minX, minY, maxZ);
            emitter.pos(1, minX, minY, minZ);
            emitter.pos(2, maxX, minY, minZ);
            emitter.pos(3, maxX, minY, maxZ);
        }

        emitter.uv(0, u0, v0)
                .uv(1, u0, v1)
                .uv(2, u1, v1)
                .uv(3, u1, v0);

        emitter.nominalFace(face)
                .cullFace(null)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private static void emitSplitEdgeCapFace(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ
    ) {
        // Endpoint caps sample both opposite edge strips so both sides of the pane remain visible.
        if (face == Direction.NORTH || face == Direction.SOUTH) {
            float midX = (minX + maxX) * 0.5F;

            emitNorthSouthEdgeCapQuad(emitter, material, face, minX, minY, minZ, midX, maxY, maxZ,
                    EDGE_START, EDGE_END);

            emitNorthSouthEdgeCapQuad(emitter, material, face, midX, minY, minZ, maxX, maxY, maxZ,
                    OPPOSITE_EDGE_START, OPPOSITE_EDGE_END);
        } else if (face == Direction.WEST || face == Direction.EAST) {
            float midZ = (minZ + maxZ) * 0.5F;

            emitWestEastEdgeCapQuad(emitter, material, face, minX, minY, minZ, maxX, maxY, midZ,
                    EDGE_START, EDGE_END);

            emitWestEastEdgeCapQuad(emitter, material, face, minX, minY, midZ, maxX, maxY, maxZ,
                    OPPOSITE_EDGE_START, OPPOSITE_EDGE_END);
        }
    }

    private static void emitNorthSouthEdgeCapQuad(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1
    ) {
        switch (face) {
            case NORTH -> {
                float z = minZ + CAP_INSET;

                emitter.pos(0, maxX, maxY, z);
                emitter.pos(1, maxX, minY, z);
                emitter.pos(2, minX, minY, z);
                emitter.pos(3, minX, maxY, z);
            }
            case SOUTH -> {
                float z = maxZ - CAP_INSET;

                emitter.pos(0, minX, maxY, z);
                emitter.pos(1, minX, minY, z);
                emitter.pos(2, maxX, minY, z);
                emitter.pos(3, maxX, maxY, z);
            }
            default -> {
                return;
            }
        }

        if (face == Direction.NORTH) {
            emitter.uv(0, u1, 0.0F)
                    .uv(1, u1, 1.0F)
                    .uv(2, u0, 1.0F)
                    .uv(3, u0, 0.0F);
        } else {
            emitter.uv(0, u0, 0.0F)
                    .uv(1, u0, 1.0F)
                    .uv(2, u1, 1.0F)
                    .uv(3, u1, 0.0F);
        }

        emitter.nominalFace(face)
                .cullFace(null)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private static void emitWestEastEdgeCapQuad(
            QuadEmitter emitter,
            Material.Baked material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1
    ) {
        switch (face) {
            case WEST -> {
                float x = minX + CAP_INSET;

                emitter.pos(0, x, maxY, minZ);
                emitter.pos(1, x, minY, minZ);
                emitter.pos(2, x, minY, maxZ);
                emitter.pos(3, x, maxY, maxZ);
            }
            case EAST -> {
                float x = maxX - CAP_INSET;

                emitter.pos(0, x, maxY, maxZ);
                emitter.pos(1, x, minY, maxZ);
                emitter.pos(2, x, minY, minZ);
                emitter.pos(3, x, maxY, minZ);
            }
            default -> {
                return;
            }
        }

        emitter.uv(0, u0, 0.0F)
                .uv(1, u0, 1.0F)
                .uv(2, u1, 1.0F)
                .uv(3, u1, 0.0F);

        emitter.nominalFace(face)
                .cullFace(null)
                .materialBake(material, MutableQuadView.BAKE_NORMALIZED)
                .diffuseShade(false)
                .emit();
    }

    private static boolean isOuterCap(Direction face, float minX, float minZ, float maxX, float maxZ) {
        // Arm boxes span from an outer block edge to the center, so only one coordinate reaches 0 or 1.
        return switch (face) {
            case NORTH -> minZ <= EPSILON;
            case SOUTH -> maxZ >= 1 - EPSILON;
            case WEST -> minX <= EPSILON;
            case EAST -> maxX >= 1 - EPSILON;
            default -> false;
        };
    }

    private static void applySideUv(
            QuadEmitter emitter,
            Direction face,
            float minX,
            float minZ,
            float maxX,
            float maxZ,
            boolean sameNorth,
            boolean sameEast,
            boolean sameSouth,
            boolean sameWest,
            boolean sameUp,
            boolean sameDown
    ) {
        float topV = sameUp ? PANE_MIN : 0;
        float bottomV = sameDown ? PANE_MAX : 1;

        if (face == Direction.NORTH) {
            emitter.uv(0, mapX(maxX, sameWest, sameEast), topV)
                    .uv(1, mapX(maxX, sameWest, sameEast), bottomV)
                    .uv(2, mapX(minX, sameWest, sameEast), bottomV)
                    .uv(3, mapX(minX, sameWest, sameEast), topV);
        } else if (face == Direction.SOUTH) {
            emitter.uv(0, mapX(minX, sameWest, sameEast), topV)
                    .uv(1, mapX(minX, sameWest, sameEast), bottomV)
                    .uv(2, mapX(maxX, sameWest, sameEast), bottomV)
                    .uv(3, mapX(maxX, sameWest, sameEast), topV);
        } else if (face == Direction.WEST) {
            emitter.uv(0, mapZ(minZ, sameNorth, sameSouth), topV)
                    .uv(1, mapZ(minZ, sameNorth, sameSouth), bottomV)
                    .uv(2, mapZ(maxZ, sameNorth, sameSouth), bottomV)
                    .uv(3, mapZ(maxZ, sameNorth, sameSouth), topV);
        } else if (face == Direction.EAST) {
            emitter.uv(0, mapZ(maxZ, sameNorth, sameSouth), topV)
                    .uv(1, mapZ(maxZ, sameNorth, sameSouth), bottomV)
                    .uv(2, mapZ(minZ, sameNorth, sameSouth), bottomV)
                    .uv(3, mapZ(minZ, sameNorth, sameSouth), topV);
        }
    }

    private static float mapX(float x, boolean sameWest, boolean sameEast) {
        if (x <= EPSILON && sameWest) return PANE_MIN;
        if (x >= 1 - EPSILON && sameEast) return PANE_MAX;
        return x;
    }

    private static float mapZ(float z, boolean sameNorth, boolean sameSouth) {
        if (z <= EPSILON && sameNorth) return PANE_MIN;
        if (z >= 1 - EPSILON && sameSouth) return PANE_MAX;
        return z;
    }

    private record PaneConnectionKey(
            boolean north,
            boolean east,
            boolean south,
            boolean west,
            boolean sameNorth,
            boolean sameEast,
            boolean sameSouth,
            boolean sameWest,
            boolean centerUp,
            boolean centerDown,
            boolean northUp,
            boolean northDown,
            boolean eastUp,
            boolean eastDown,
            boolean southUp,
            boolean southDown,
            boolean westUp,
            boolean westDown
    ) {
    }
}
