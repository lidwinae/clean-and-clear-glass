package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.List;

public final class VanillaConnectedGlassModel
        extends DelegateBlockStateModel {
    private static final float T = 1.0F / 16.0F;
    private static final float LAYERED_CENTER_DEPTH =
            1.0F / 1024.0F;
    private static final float CENTER_UV_MIN = 2.0F / 16.0F;
    private static final float CENTER_UV_MAX = 14.0F / 16.0F;

    private final TextureAtlasSprite edgeMaterial;
    private final TextureAtlasSprite centerMaterial;
    private final boolean useFullCenterTexture;

    public VanillaConnectedGlassModel(
            BlockStateModel wrapped,
            TextureAtlasSprite material
    ) {
        this(wrapped, material, material, false);
    }

    public VanillaConnectedGlassModel(
            BlockStateModel wrapped,
            TextureAtlasSprite edgeMaterial,
            TextureAtlasSprite centerMaterial,
            boolean useFullCenterTexture
    ) {
        super(wrapped);
        this.edgeMaterial = edgeMaterial;
        this.centerMaterial = centerMaterial;
        this.useFullCenterTexture = useFullCenterTexture;
    }

    @Override
    public void collectParts(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> parts
    ) {
        QuadCollection.Builder builder =
                new QuadCollection.Builder();

        NeoQuadEmitter emitter = new NeoQuadEmitter(builder);

        boolean up = connects(level, pos.above(), state);
        boolean down = connects(level, pos.below(), state);
        boolean north = connects(level, pos.north(), state);
        boolean east = connects(level, pos.east(), state);
        boolean south = connects(level, pos.south(), state);
        boolean west = connects(level, pos.west(), state);

        if (!north) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.NORTH,
                    east,
                    down,
                    west,
                    up,
                    useFullCenterTexture
            );
        }

        if (!east) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.EAST,
                    south,
                    down,
                    north,
                    up,
                    useFullCenterTexture
            );
        }

        if (!south) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.SOUTH,
                    west,
                    down,
                    east,
                    up,
                    useFullCenterTexture
            );
        }

        if (!west) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.WEST,
                    north,
                    down,
                    south,
                    up,
                    useFullCenterTexture
            );
        }

        if (!up) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.UP,
                    west,
                    south,
                    east,
                    north,
                    useFullCenterTexture
            );
        }

        if (!down) {
            emitCenter(
                    emitter,
                    centerMaterial,
                    Direction.DOWN,
                    west,
                    north,
                    east,
                    south,
                    useFullCenterTexture
            );
        }

        if (!north) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.NORTH,
                    east,
                    down,
                    west,
                    up
            );
        }

        if (!east) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.EAST,
                    south,
                    down,
                    north,
                    up
            );
        }

        if (!south) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.SOUTH,
                    west,
                    down,
                    east,
                    up
            );
        }

        if (!west) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.WEST,
                    north,
                    down,
                    south,
                    up
            );
        }

        if (!up) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.UP,
                    west,
                    south,
                    east,
                    north
            );
        }

        if (!down) {
            emitBorders(
                    emitter,
                    edgeMaterial,
                    Direction.DOWN,
                    west,
                    north,
                    east,
                    south
            );
        }

        parts.add(
                emitter.buildPart(
                        particleMaterial(level, pos, state)
                )
        );
    }

    @Override
    public Object createGeometryKey(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random
    ) {
        return new ConnectionKey(
                this,
                connects(level, pos.above(), state),
                connects(level, pos.below(), state),
                connects(level, pos.north(), state),
                connects(level, pos.east(), state),
                connects(level, pos.south(), state),
                connects(level, pos.west(), state)
        );
    }

    private static boolean connects(
            BlockAndTintGetter level,
            BlockPos position,
            BlockState state
    ) {
        return level.getBlockState(position).getBlock()
                == state.getBlock();
    }

    private static void emitBorders(
            NeoQuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            boolean connectLeft,
            boolean connectBottom,
            boolean connectRight,
            boolean connectTop
    ) {
        boolean drawBottom = !connectBottom;
        boolean drawTop = !connectTop;

        if (drawBottom) {
            emitStrip(
                    emitter,
                    material,
                    face,
                    0,
                    0,
                    1,
                    T,
                    0
            );
        }

        if (drawTop) {
            emitStrip(
                    emitter,
                    material,
                    face,
                    0,
                    1 - T,
                    1,
                    1,
                    0
            );
        }

        float sideBottom = drawBottom ? T : 0;
        float sideTop = drawTop ? 1 - T : 1;

        if (!connectLeft) {
            emitStrip(
                    emitter,
                    material,
                    face,
                    0,
                    sideBottom,
                    T,
                    sideTop,
                    0
            );
        }

        if (!connectRight) {
            emitStrip(
                    emitter,
                    material,
                    face,
                    1 - T,
                    sideBottom,
                    1,
                    sideTop,
                    0
            );
        }
    }

    private static void emitStrip(
            NeoQuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        emitter.square(
                        face,
                        left,
                        bottom,
                        right,
                        top,
                        depth
                )
                .spriteBake(
                        material,
                        NeoQuadEmitter.BAKE_LOCK_UV
                )
                .diffuseShade(false)
                .emit();
    }

    private static void emitCenter(
            NeoQuadEmitter emitter,
            TextureAtlasSprite material,
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

        float uvLeft =
                fullTexture ? 0 : CENTER_UV_MIN;
        float uvBottom =
                fullTexture ? 0 : CENTER_UV_MIN;
        float uvRight =
                fullTexture ? 1 : CENTER_UV_MAX;
        float uvTop =
                fullTexture ? 1 : CENTER_UV_MAX;

        float depth =
                fullTexture ? LAYERED_CENTER_DEPTH : 0;

        emitter.square(
                        face,
                        left,
                        bottom,
                        right,
                        top,
                        depth
                )
                .uv(0, uvLeft, uvBottom)
                .uv(1, uvLeft, uvTop)
                .uv(2, uvRight, uvTop)
                .uv(3, uvRight, uvBottom)
                .spriteBake(
                        material,
                        NeoQuadEmitter.BAKE_NORMALIZED
                )
                .diffuseShade(false)
                .emit();
    }

    private record ConnectionKey(
            Object modelIdentity,
            boolean up,
            boolean down,
            boolean north,
            boolean east,
            boolean south,
            boolean west
    ) {
    }
}
