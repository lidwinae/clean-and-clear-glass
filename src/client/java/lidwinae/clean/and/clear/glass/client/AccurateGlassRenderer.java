package lidwinae.clean.and.clear.glass.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Draws partially transparent C&CG glass outside Sodium's fixed-order chunk
 * buffers. Vanilla's transient translucent buffer generates a camera-relative
 * quad index order on upload, which avoids the NORTH/EAST versus SOUTH/WEST
 * artifact present in Sodium 0.5.13.
 */
public final class AccurateGlassRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Clean and Clear Glass");
    private static final ConcurrentMap<Long, CachedBlock> BLOCKS = new ConcurrentHashMap<>();
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final AtomicBoolean CAPTURE_REPORTED = new AtomicBoolean();
    private static final AtomicBoolean DRAW_REPORTED = new AtomicBoolean();
    private static final BufferBuilder BUFFER = new BufferBuilder(RenderType.BIG_BUFFER_SIZE);
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(BUFFER);

    private static volatile ClientLevel trackedLevel;

    private AccurateGlassRenderer() {
    }

    public static void register() {
        if (!SodiumModelSupport.isSupportedSodiumLoaded() || !REGISTERED.compareAndSet(false, true)) {
            return;
        }

        ClientTickEvents.START_CLIENT_TICK.register(client -> updateLevel(client.level));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(AccurateGlassRenderer::render);
        LOGGER.info("Enabled accurate glass transparency sorting for Sodium 0.5.13");
    }

    public static void capture(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            long seed
    ) {
        ClientLevel clientLevel = Minecraft.getInstance().level;

        if (clientLevel == null) {
            return;
        }

        updateLevel(clientLevel);
        List<BakedQuad> quads = SodiumQuadAdapter.getAllQuads(model, level, state, pos, seed);
        BLOCKS.put(pos.asLong(), new CachedBlock(clientLevel, pos.immutable(), state, quads));

        if (!quads.isEmpty() && CAPTURE_REPORTED.compareAndSet(false, true)) {
            LOGGER.info("Captured accurate glass geometry for the active world");
        }
    }

    public static void clear() {
        BLOCKS.clear();
    }

    private static synchronized void updateLevel(ClientLevel level) {
        if (trackedLevel != level) {
            clear();
            trackedLevel = level;
            CAPTURE_REPORTED.set(false);
            DRAW_REPORTED.set(false);
        }
    }

    private static void render(WorldRenderContext context) {
        ClientLevel level = context.world();

        if (level == null || level != trackedLevel || BLOCKS.isEmpty()) {
            return;
        }

        Vec3 camera = context.camera().getPosition();
        PoseStack poseStack = context.matrixStack();
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(RenderType.translucent());
        int submittedQuads = 0;

        for (CachedBlock cached : BLOCKS.values()) {
            if (cached.level != level || !level.hasChunkAt(cached.pos)) {
                BLOCKS.remove(cached.pos.asLong(), cached);
                continue;
            }

            BlockState currentState = level.getBlockState(cached.pos);

            // Keep the previous pane geometry visible while Sodium asynchronously
            // rebuilds the same block with new connection properties. capture()
            // replaces this entry as soon as the rebuilt geometry is ready.
            if (currentState.getBlock() != cached.state.getBlock()) {
                BLOCKS.remove(cached.pos.asLong(), cached);
                continue;
            }

            if (context.frustum() != null && !context.frustum().isVisible(new AABB(cached.pos))) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(
                    cached.pos.getX() - camera.x,
                    cached.pos.getY() - camera.y,
                    cached.pos.getZ() - camera.z
            );

            for (BakedQuad quad : cached.quads) {
                // Sample the light immediately outside each glass surface. This
                // avoids tinted glass darkening itself by sampling its own
                // light-blocking block position.
                int light = LevelRenderer.getLightColor(
                        level,
                        cached.pos.relative(quad.getDirection())
                );

                consumer.putBulkData(
                        poseStack.last(),
                        quad,
                        1.0F,
                        1.0F,
                        1.0F,
                        light,
                        OverlayTexture.NO_OVERLAY
                );
                submittedQuads++;
            }

            poseStack.popPose();
        }

        BUFFER_SOURCE.endBatch(RenderType.translucent());

        if (submittedQuads > 0 && DRAW_REPORTED.compareAndSet(false, true)) {
            LOGGER.info("Submitted {} accurate glass quads with the world camera matrix", submittedQuads);
        }
    }

    private static final class CachedBlock {
        private final ClientLevel level;
        private final BlockPos pos;
        private final BlockState state;
        private final List<BakedQuad> quads;

        private CachedBlock(ClientLevel level, BlockPos pos, BlockState state, List<BakedQuad> quads) {
            this.level = level;
            this.pos = pos;
            this.state = state;
            this.quads = quads;
        }
    }
}
