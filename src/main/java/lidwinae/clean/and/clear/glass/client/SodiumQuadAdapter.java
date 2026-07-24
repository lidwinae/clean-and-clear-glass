package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SodiumQuadAdapter {
    private static final Logger LOGGER = LogManager.getLogger("Clean and Clear Glass");
    private static final AtomicBoolean BACKEND_REPORTED = new AtomicBoolean();
    private static final int UNCULLED_INDEX = Direction.values().length;
    private static final ThreadLocal<QuadCache> CACHE = new ThreadLocal<QuadCache>() {
        @Override
        protected QuadCache initialValue() {
            return new QuadCache();
        }
    };
    private static final ThreadLocal<RenderCall> RENDER_CALL = new ThreadLocal<RenderCall>();

    private SodiumQuadAdapter() {
    }

    public static void beginBlockRender(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter world,
            BlockState state,
            BlockPos pos,
            long seed
    ) {
        CACHE.get().invalidate();
        RENDER_CALL.set(new RenderCall(model, world, state, pos.immutable(), seed));
    }

    public static void endBlockRender() {
        RENDER_CALL.remove();
    }

    public static List<BakedQuad> currentQuads(
            SodiumCompatibleGlassModel model,
            BlockState state,
            Direction cullFace
    ) {
        RenderCall call = RENDER_CALL.get();

        if (call == null || call.model != model || call.state != state) {
            return null;
        }

        return getQuads(model, call.world, state, call.pos, call.seed, cullFace);
    }

    public static List<BakedQuad> getQuads(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter world,
            BlockState state,
            BlockPos pos,
            long seed,
            Direction cullFace
    ) {
        reportBackend();
        QuadCache cache = CACHE.get();

        if (!cache.matches(model, state, pos, seed)) {
            cache.rebuild(model, world, state, pos, seed);
        }

        return cache.quads[index(cullFace)];
    }

    public static List<BakedQuad> getAllQuads(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter world,
            BlockState state,
            BlockPos pos,
            long seed
    ) {
        reportBackend();
        QuadCache cache = CACHE.get();

        if (!cache.matches(model, state, pos, seed)) {
            cache.rebuild(model, world, state, pos, seed);
        }

        return cache.allQuads;
    }

    private static void reportBackend() {
        if (BACKEND_REPORTED.compareAndSet(false, true)) {
            LOGGER.info("Using the direct Sodium 0.2.0 connected-glass backend with accurate transparency sorting");
        }
    }

    private static int index(Direction face) {
        return face == null ? UNCULLED_INDEX : face.ordinal();
    }

    private static final class RenderCall {
        private final SodiumCompatibleGlassModel model;
        private final BlockAndTintGetter world;
        private final BlockState state;
        private final BlockPos pos;
        private final long seed;

        private RenderCall(
                SodiumCompatibleGlassModel model,
                BlockAndTintGetter world,
                BlockState state,
                BlockPos pos,
                long seed
        ) {
            this.model = model;
            this.world = world;
            this.state = state;
            this.pos = pos;
            this.seed = seed;
        }
    }

    private static final class QuadCache {
        private SodiumCompatibleGlassModel model;
        private BlockState state;
        private long pos;
        private long seed;
        private final List<BakedQuad>[] quads;
        private List<BakedQuad> allQuads = Collections.emptyList();

        @SuppressWarnings("unchecked")
        private QuadCache() {
            quads = (List<BakedQuad>[]) new List<?>[Direction.values().length + 1];

            for (int i = 0; i < quads.length; i++) {
                quads[i] = Collections.emptyList();
            }
        }

        private boolean matches(SodiumCompatibleGlassModel model, BlockState state, BlockPos pos, long seed) {
            return this.model == model
                    && this.state == state
                    && this.pos == pos.asLong()
                    && this.seed == seed;
        }

        private void rebuild(
                SodiumCompatibleGlassModel model,
                BlockAndTintGetter world,
                BlockState state,
                BlockPos pos,
                long seed
        ) {
            @SuppressWarnings("unchecked")
            List<BakedQuad>[] generated = (List<BakedQuad>[]) new List<?>[Direction.values().length + 1];

            for (int i = 0; i < generated.length; i++) {
                generated[i] = new ArrayList<BakedQuad>();
            }

            CapturingRenderContext context = new CapturingRenderContext();
            Supplier<Random> randomSupplier = () -> new Random(seed);
            model.emitBlockQuads(world, state, pos, randomSupplier, context);
            Mesh mesh = context.finish();

            mesh.forEach(quad -> generated[index(quad.cullFace())].add(
                    quad.toBakedQuad(0, model.cleanAndClearGlass$spriteForTag(quad.tag()), false)
            ));

            List<BakedQuad> flattened = new ArrayList<BakedQuad>();

            for (int i = 0; i < generated.length; i++) {
                quads[i] = Collections.unmodifiableList(new ArrayList<BakedQuad>(generated[i]));
                flattened.addAll(generated[i]);
            }

            allQuads = Collections.unmodifiableList(flattened);
            this.model = model;
            this.state = state;
            this.pos = pos.asLong();
            this.seed = seed;
        }

        private void invalidate() {
            model = null;
            state = null;
            allQuads = Collections.emptyList();
        }
    }

    private static final class CapturingRenderContext implements RenderContext {
        private final MeshBuilder meshBuilder = IndigoRenderer.INSTANCE.meshBuilder();

        @Override
        public Consumer<Mesh> meshConsumer() {
            return mesh -> {
                throw new UnsupportedOperationException("C&CG only captures dynamic glass quads");
            };
        }

        @Override
        public Consumer<BakedModel> fallbackConsumer() {
            return model -> {
                throw new UnsupportedOperationException("C&CG only captures dynamic glass quads");
            };
        }

        @Override
        public QuadEmitter getEmitter() {
            return meshBuilder.getEmitter();
        }

        @Override
        public void pushTransform(QuadTransform transform) {
            throw new UnsupportedOperationException("C&CG does not use quad transforms");
        }

        @Override
        public void popTransform() {
            throw new UnsupportedOperationException("C&CG does not use quad transforms");
        }

        private Mesh finish() {
            return meshBuilder.build();
        }
    }
}
