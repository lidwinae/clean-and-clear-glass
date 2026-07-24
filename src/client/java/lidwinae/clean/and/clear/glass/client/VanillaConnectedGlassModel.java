package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.function.Supplier;

public final class VanillaConnectedGlassModel extends ForwardingBakedModel {
    private static final RenderMaterial NO_DIFFUSE_MATERIAL = Objects.requireNonNull(
            RendererAccess.INSTANCE.getRenderer(),
            "Clean and Clear Glass requires a Fabric renderer"
    ).materialFinder().disableDiffuse(true).find();

    private static final float T = 1.0F / 16.0F;
    private static final float LAYERED_CENTER_DEPTH = 1.0F / 1024.0F;
    private static final float CENTER_UV_MIN = 2.0F / 16.0F;
    private static final float CENTER_UV_MAX = 14.0F / 16.0F;
    private static final int WHITE = 0xFFFFFFFF;

    private final TextureAtlasSprite edgeMaterial;
    private final TextureAtlasSprite centerMaterial;
    private final boolean useFullCenterTexture;

    public VanillaConnectedGlassModel(BakedModel wrapped, TextureAtlasSprite material) {
        this(wrapped, material, material, false);
    }

    public VanillaConnectedGlassModel(
            BakedModel wrapped,
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
    public boolean isVanillaAdapter() {
        return false;
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

        if (!north) emitFaceBorders(emitter, edgeMaterial, Direction.NORTH, east, down, west, up);
        if (!east) emitFaceBorders(emitter, edgeMaterial, Direction.EAST, south, down, north, up);
        if (!south) emitFaceBorders(emitter, edgeMaterial, Direction.SOUTH, west, down, east, up);
        if (!west) emitFaceBorders(emitter, edgeMaterial, Direction.WEST, north, down, south, up);
        if (!up) emitFaceBorders(emitter, edgeMaterial, Direction.UP, west, south, east, north);
        if (!down) emitFaceBorders(emitter, edgeMaterial, Direction.DOWN, west, north, east, south);
    }

    private static boolean connectsToGlass(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos).getBlock() == state.getBlock();
    }

    private static void emitFaceBorders(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            boolean connectLeft,
            boolean connectBottom,
            boolean connectRight,
            boolean connectTop
    ) {
        boolean drawBottom = !connectBottom;
        boolean drawTop = !connectTop;

        // Horizontal strips memiliki area sudut.
        if (drawBottom) {
            emitStrip(emitter, material, face, 0, 0, 1, T, 0);
        }

        if (drawTop) {
            emitStrip(emitter, material, face, 0, 1 - T, 1, 1, 0);
        }

        // Jika horizontal strip ada, side strip berhenti tepat di batasnya.
        // Jika horizontal strip tidak ada, side strip memanjang sampai ujung face.
        float sideBottom = drawBottom ? T : 0;
        float sideTop = drawTop ? 1 - T : 1;

        if (!connectLeft) {
            emitStrip(emitter, material, face, 0, sideBottom, T, sideTop, 0);
        }

        if (!connectRight) {
            emitStrip(emitter, material, face, 1 - T, sideBottom, 1, sideTop, 0);
        }
    }

    private static void emitStrip(
            QuadEmitter emitter,
            TextureAtlasSprite material,
            Direction face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        emitter.square(face, left, bottom, right, top, depth)
                .spriteBake(material, MutableQuadView.BAKE_LOCK_UV)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(NO_DIFFUSE_MATERIAL)
                .emit();
    }

    private static void emitCenter(
            QuadEmitter emitter,
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
        float uvLeft = fullTexture ? 0 : CENTER_UV_MIN;
        float uvBottom = fullTexture ? 0 : CENTER_UV_MIN;
        float uvRight = fullTexture ? 1 : CENTER_UV_MAX;
        float uvTop = fullTexture ? 1 : CENTER_UV_MAX;
        float depth = fullTexture ? LAYERED_CENTER_DEPTH : 0;

        emitter.square(face, left, bottom, right, top, depth)
                .uv(0, uvLeft, uvBottom)
                .uv(1, uvLeft, uvTop)
                .uv(2, uvRight, uvTop)
                .uv(3, uvRight, uvBottom)
                .spriteBake(material, MutableQuadView.BAKE_NORMALIZED)
                .color(WHITE, WHITE, WHITE, WHITE)
                .material(NO_DIFFUSE_MATERIAL)
                .emit();
    }
}
