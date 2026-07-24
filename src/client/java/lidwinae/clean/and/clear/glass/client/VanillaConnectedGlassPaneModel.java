package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public final class VanillaConnectedGlassPaneModel extends ForwardingBakedModel implements SodiumCompatibleGlassModel {
    private static final int CENTER_SPRITE_TAG = 1;

    // Vanilla pane thickness is two pixels wide, centered at 8/16.
    private static final float PANE_MIN = 7.0F / 16.0F;
    private static final float PANE_MAX = 9.0F / 16.0F;
    private static final float EPSILON = 0.0001F;

    // Pull endpoint caps slightly inward so they remain visible against adjacent blocks.
    private static final float CAP_INSET = 1.0F / 1024.0F;
    private static final float CENTER_LAYER_OFFSET = 1.0F / 2048.0F;

    // One texture pixel from each side of the 16x16 glass texture.
    private static final float EDGE_STRIP = 1.0F / 16.0F;
    private static final float EDGE_START = 0.0F;
    private static final float EDGE_END = EDGE_STRIP;
    private static final float OPPOSITE_EDGE_START = 1.0F - EDGE_STRIP;
    private static final float OPPOSITE_EDGE_END = 1.0F;
    private static final int WHITE = 0xFFFFFFFF;

    // Vertically connected pane bodies meet on the same interior texel center.
    private static final float VERTICAL_SEAM_V = 8.5F / 16.0F;

    private final TextureAtlasSprite edgeMaterial;
    private final TextureAtlasSprite centerMaterial;
    private final boolean requiresAccurateTransparency;

    public VanillaConnectedGlassPaneModel(
            BakedModel wrapped,
            TextureAtlasSprite material,
            boolean requiresAccurateTransparency
    ) {
        this(wrapped, material, null, requiresAccurateTransparency);
    }

    public VanillaConnectedGlassPaneModel(
            BakedModel wrapped,
            TextureAtlasSprite edgeMaterial,
            TextureAtlasSprite centerMaterial,
            boolean requiresAccurateTransparency
    ) {
        this.wrapped = wrapped;
        this.edgeMaterial = edgeMaterial;
        this.centerMaterial = centerMaterial;
        this.requiresAccurateTransparency = requiresAccurateTransparency;
    }

    @Override
    public boolean isVanillaAdapter() {
        return SodiumModelSupport.isSodiumLoaded();
    }

    @Override
    public TextureAtlasSprite cleanAndClearGlass$spriteForTag(int tag) {
        return tag == CENTER_SPRITE_TAG && centerMaterial != null ? centerMaterial : edgeMaterial;
    }

    @Override
    public boolean cleanAndClearGlass$requiresAccurateTransparency() {
        return requiresAccurateTransparency;
    }

    @Override
    public void emitBlockQuads(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            Supplier<RandomSource> randomSupplier,
            RenderContext context
    ) {
        QuadEmitter emitter = context.getEmitter();

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

        if (centerMaterial != null) {
            emitCenterOverlayBox(
                    emitter,
                    centerMaterial,
                    PANE_MIN,
                    PANE_MIN,
                    PANE_MAX,
                    PANE_MAX,
                    showCenterNorth,
                    showCenterEast,
                    showCenterSouth,
                    showCenterWest
            );

            if (north) {
                emitCenterOverlayBox(
                        emitter,
                        centerMaterial,
                        PANE_MIN,
                        0,
                        PANE_MAX,
                        PANE_MIN,
                        false,
                        true,
                        false,
                        true
                );
            }

            if (east) {
                emitCenterOverlayBox(
                        emitter,
                        centerMaterial,
                        PANE_MAX,
                        PANE_MIN,
                        1,
                        PANE_MAX,
                        true,
                        false,
                        true,
                        false
                );
            }

            if (south) {
                emitCenterOverlayBox(
                        emitter,
                        centerMaterial,
                        PANE_MIN,
                        PANE_MAX,
                        PANE_MAX,
                        1,
                        false,
                        true,
                        false,
                        true
                );
            }

            if (west) {
                emitCenterOverlayBox(
                        emitter,
                        centerMaterial,
                        0,
                        PANE_MIN,
                        PANE_MIN,
                        PANE_MAX,
                        true,
                        false,
                        true,
                        false
                );
            }
        }

        emitBox(emitter, edgeMaterial, PANE_MIN, 0, PANE_MIN, PANE_MAX, 1, PANE_MAX,
                showCenterNorth,
                showCenterEast,
                showCenterSouth,
                showCenterWest,
                sameNorth, sameEast, sameSouth, sameWest,
                centerUp, centerDown);

        if (north) {
            emitBox(emitter, edgeMaterial, PANE_MIN, 0, 0, PANE_MAX, 1, PANE_MIN,
                    !sameNorth, true, false, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    northUp, northDown);
        }

        if (east) {
            emitBox(emitter, edgeMaterial, PANE_MAX, 0, PANE_MIN, 1, 1, PANE_MAX,
                    true, !sameEast, true, false,
                    sameNorth, sameEast, sameSouth, sameWest,
                    eastUp, eastDown);
        }

        if (south) {
            emitBox(emitter, edgeMaterial, PANE_MIN, 0, PANE_MAX, PANE_MAX, 1, 1,
                    false, true, !sameSouth, true,
                    sameNorth, sameEast, sameSouth, sameWest,
                    southUp, southDown);
        }

        if (west) {
            emitBox(emitter, edgeMaterial, 0, 0, PANE_MIN, PANE_MIN, 1, PANE_MAX,
                    true, false, true, !sameWest,
                    sameNorth, sameEast, sameSouth, sameWest,
                    westUp, westDown);
        }
    }

    private static void emitCenterOverlayBox(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            float minX,
            float minZ,
            float maxX,
            float maxZ,
            boolean showNorth,
            boolean showEast,
            boolean showSouth,
            boolean showWest
    ) {
        if (showNorth) emitCenterOverlayFace(emitter, material, Direction.NORTH, minX, minZ, maxX, maxZ);
        if (showEast) emitCenterOverlayFace(emitter, material, Direction.EAST, minX, minZ, maxX, maxZ);
        if (showSouth) emitCenterOverlayFace(emitter, material, Direction.SOUTH, minX, minZ, maxX, maxZ);
        if (showWest) emitCenterOverlayFace(emitter, material, Direction.WEST, minX, minZ, maxX, maxZ);
    }

    private static void emitCenterOverlayFace(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minZ,
            float maxX,
            float maxZ
    ) {
        switch (face) {
            case NORTH -> {
                float z = minZ + CENTER_LAYER_OFFSET;
                emitter.pos(0, maxX, 1, z);
                emitter.pos(1, maxX, 0, z);
                emitter.pos(2, minX, 0, z);
                emitter.pos(3, minX, 1, z);
                emitter.uv(0, maxX, 0)
                        .uv(1, maxX, 1)
                        .uv(2, minX, 1)
                        .uv(3, minX, 0);
            }
            case SOUTH -> {
                float z = maxZ - CENTER_LAYER_OFFSET;
                emitter.pos(0, minX, 1, z);
                emitter.pos(1, minX, 0, z);
                emitter.pos(2, maxX, 0, z);
                emitter.pos(3, maxX, 1, z);
                emitter.uv(0, minX, 0)
                        .uv(1, minX, 1)
                        .uv(2, maxX, 1)
                        .uv(3, maxX, 0);
            }
            case WEST -> {
                float x = minX + CENTER_LAYER_OFFSET;
                emitter.pos(0, x, 1, minZ);
                emitter.pos(1, x, 0, minZ);
                emitter.pos(2, x, 0, maxZ);
                emitter.pos(3, x, 1, maxZ);
                emitter.uv(0, minZ, 0)
                        .uv(1, minZ, 1)
                        .uv(2, maxZ, 1)
                        .uv(3, maxZ, 0);
            }
            case EAST -> {
                float x = maxX - CENTER_LAYER_OFFSET;
                emitter.pos(0, x, 1, maxZ);
                emitter.pos(1, x, 0, maxZ);
                emitter.pos(2, x, 0, minZ);
                emitter.pos(3, x, 1, minZ);
                emitter.uv(0, maxZ, 0)
                        .uv(1, maxZ, 1)
                        .uv(2, minZ, 1)
                        .uv(3, minZ, 0);
            }
            default -> {
                return;
            }
        }

        emitter.nominalFace(face)
                .cullFace(null)
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .tag(CENTER_SPRITE_TAG)
                .material(SodiumModelSupport.noDiffuseMaterial(emitter))
                .emit();
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
            TextureAtlasSprite material,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            boolean showNorth,
            boolean showEast,
            boolean showSouth,
            boolean showWest,
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

        if (!sameUp) {
            emitTopBottomFace(emitter, material, Direction.UP, minX, minY, minZ, maxX, maxY, maxZ);
        }

        if (!sameDown) {
            emitTopBottomFace(emitter, material, Direction.DOWN, minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private static void emitFace(
            QuadEmitter emitter,
            TextureAtlasSprite material,
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
            emitSplitEdgeCapFace(
                    emitter,
                    material,
                    face,
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    sameUp,
                    sameDown
            );
            return;
        }

        // Keep visible top and bottom borders exactly one block pixel high.
        // Only the center band stretches when a matching pane continues vertically.
        float bodyMinY = sameDown ? minY : minY + EDGE_STRIP;
        float bodyMaxY = sameUp ? maxY : maxY - EDGE_STRIP;
        float bodyTopV = sameUp ? VERTICAL_SEAM_V : EDGE_END;
        float bodyBottomV = sameDown ? VERTICAL_SEAM_V : OPPOSITE_EDGE_START;

        emitSideFaceQuad(
                emitter,
                material,
                face,
                minX,
                bodyMinY,
                minZ,
                maxX,
                bodyMaxY,
                maxZ,
                sameNorth,
                sameEast,
                sameSouth,
                sameWest,
                bodyTopV,
                bodyBottomV
        );

        if (!sameUp) {
            emitSideFaceQuad(
                    emitter,
                    material,
                    face,
                    minX,
                    maxY - EDGE_STRIP,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    sameNorth,
                    sameEast,
                    sameSouth,
                    sameWest,
                    EDGE_START,
                    EDGE_END
            );
        }

        if (!sameDown) {
            emitSideFaceQuad(
                    emitter,
                    material,
                    face,
                    minX,
                    minY,
                    minZ,
                    maxX,
                    minY + EDGE_STRIP,
                    maxZ,
                    sameNorth,
                    sameEast,
                    sameSouth,
                    sameWest,
                    OPPOSITE_EDGE_START,
                    OPPOSITE_EDGE_END
            );
        }
    }

    private static void emitSideFaceQuad(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            boolean sameNorth,
            boolean sameEast,
            boolean sameSouth,
            boolean sameWest,
            float topV,
            float bottomV
    ) {
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

        applySideUv(
                emitter,
                face,
                minX,
                minZ,
                maxX,
                maxZ,
                sameNorth,
                sameEast,
                sameSouth,
                sameWest,
                topV,
                bottomV
        );

        emitter.nominalFace(face)
                .cullFace(null)
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(SodiumModelSupport.noDiffuseMaterial(emitter))
                .emit();
    }

    private static void emitTopBottomFace(
            QuadEmitter emitter,
            TextureAtlasSprite material,
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
            TextureAtlasSprite material,
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
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(SodiumModelSupport.noDiffuseMaterial(emitter))
                .emit();
    }

    private static void emitSplitEdgeCapFace(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            boolean sameUp,
            boolean sameDown
    ) {
        // Endpoint caps sample both opposite edge strips so both sides of the pane remain visible.
        if (face == Direction.NORTH || face == Direction.SOUTH) {
            float midX = (minX + maxX) * 0.5F;

            emitNorthSouthEdgeCapBands(
                    emitter,
                    material,
                    face,
                    minX,
                    minY,
                    minZ,
                    midX,
                    maxY,
                    maxZ,
                    EDGE_START,
                    EDGE_END,
                    sameUp,
                    sameDown
            );

            emitNorthSouthEdgeCapBands(
                    emitter,
                    material,
                    face,
                    midX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    maxZ,
                    OPPOSITE_EDGE_START,
                    OPPOSITE_EDGE_END,
                    sameUp,
                    sameDown
            );
        } else if (face == Direction.WEST || face == Direction.EAST) {
            float midZ = (minZ + maxZ) * 0.5F;

            emitWestEastEdgeCapBands(
                    emitter,
                    material,
                    face,
                    minX,
                    minY,
                    minZ,
                    maxX,
                    maxY,
                    midZ,
                    EDGE_START,
                    EDGE_END,
                    sameUp,
                    sameDown
            );

            emitWestEastEdgeCapBands(
                    emitter,
                    material,
                    face,
                    minX,
                    minY,
                    midZ,
                    maxX,
                    maxY,
                    maxZ,
                    OPPOSITE_EDGE_START,
                    OPPOSITE_EDGE_END,
                    sameUp,
                    sameDown
            );
        }
    }

    private static void emitNorthSouthEdgeCapBands(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1,
            boolean sameUp,
            boolean sameDown
    ) {
        float bodyMinY = sameDown ? minY : minY + EDGE_STRIP;
        float bodyMaxY = sameUp ? maxY : maxY - EDGE_STRIP;
        float bodyTopV = sameUp ? VERTICAL_SEAM_V : EDGE_END;
        float bodyBottomV = sameDown ? VERTICAL_SEAM_V : OPPOSITE_EDGE_START;

        emitNorthSouthEdgeCapQuad(
                emitter, material, face,
                minX, bodyMinY, minZ,
                maxX, bodyMaxY, maxZ,
                u0, u1, bodyTopV, bodyBottomV
        );

        if (!sameUp) {
            emitNorthSouthEdgeCapQuad(
                    emitter, material, face,
                    minX, maxY - EDGE_STRIP, minZ,
                    maxX, maxY, maxZ,
                    u0, u1, EDGE_START, EDGE_END
            );
        }

        if (!sameDown) {
            emitNorthSouthEdgeCapQuad(
                    emitter, material, face,
                    minX, minY, minZ,
                    maxX, minY + EDGE_STRIP, maxZ,
                    u0, u1, OPPOSITE_EDGE_START, OPPOSITE_EDGE_END
            );
        }
    }

    private static void emitNorthSouthEdgeCapQuad(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1,
            float topV,
            float bottomV
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
            emitter.uv(0, u1, topV)
                    .uv(1, u1, bottomV)
                    .uv(2, u0, bottomV)
                    .uv(3, u0, topV);
        } else {
            emitter.uv(0, u0, topV)
                    .uv(1, u0, bottomV)
                    .uv(2, u1, bottomV)
                    .uv(3, u1, topV);
        }

        emitter.nominalFace(face)
                .cullFace(null)
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(SodiumModelSupport.noDiffuseMaterial(emitter))
                .emit();
    }

    private static void emitWestEastEdgeCapBands(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1,
            boolean sameUp,
            boolean sameDown
    ) {
        float bodyMinY = sameDown ? minY : minY + EDGE_STRIP;
        float bodyMaxY = sameUp ? maxY : maxY - EDGE_STRIP;
        float bodyTopV = sameUp ? VERTICAL_SEAM_V : EDGE_END;
        float bodyBottomV = sameDown ? VERTICAL_SEAM_V : OPPOSITE_EDGE_START;

        emitWestEastEdgeCapQuad(
                emitter, material, face,
                minX, bodyMinY, minZ,
                maxX, bodyMaxY, maxZ,
                u0, u1, bodyTopV, bodyBottomV
        );

        if (!sameUp) {
            emitWestEastEdgeCapQuad(
                    emitter, material, face,
                    minX, maxY - EDGE_STRIP, minZ,
                    maxX, maxY, maxZ,
                    u0, u1, EDGE_START, EDGE_END
            );
        }

        if (!sameDown) {
            emitWestEastEdgeCapQuad(
                    emitter, material, face,
                    minX, minY, minZ,
                    maxX, minY + EDGE_STRIP, maxZ,
                    u0, u1, OPPOSITE_EDGE_START, OPPOSITE_EDGE_END
            );
        }
    }

    private static void emitWestEastEdgeCapQuad(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float u0,
            float u1,
            float topV,
            float bottomV
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

        emitter.uv(0, u0, topV)
                .uv(1, u0, bottomV)
                .uv(2, u1, bottomV)
                .uv(3, u1, topV);

        emitter.nominalFace(face)
                .cullFace(null)
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(SodiumModelSupport.noDiffuseMaterial(emitter))
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
            float topV,
            float bottomV
    ) {
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

}
