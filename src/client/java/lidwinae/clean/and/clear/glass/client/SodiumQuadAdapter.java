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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SodiumQuadAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("Clean and Clear Glass");
    private static final AtomicBoolean BACKEND_REPORTED = new AtomicBoolean();
    private static final int UNCULLED_INDEX = Direction.values().length;
    private static final ThreadLocal<QuadCache> CACHE = ThreadLocal.withInitial(QuadCache::new);

    private SodiumQuadAdapter() {
    }

    public static void beginBlockRender() {
        CACHE.get().invalidate();
    }

    public static List<BakedQuad> getQuads(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            long seed,
            Direction cullFace
    ) {
        reportBackend();

        QuadCache cache = CACHE.get();

        if (!cache.matches(model, state, pos, seed)) {
            cache.rebuild(model, level, state, pos, seed);
        }

        return cache.quads[index(cullFace)];
    }

    public static List<BakedQuad> getAllQuads(
            SodiumCompatibleGlassModel model,
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            long seed
    ) {
        reportBackend();
        QuadCache cache = CACHE.get();

        if (!cache.matches(model, state, pos, seed)) {
            cache.rebuild(model, level, state, pos, seed);
        }

        return cache.allQuads;
    }

    private static void reportBackend() {
        if (BACKEND_REPORTED.compareAndSet(false, true)) {
            LOGGER.info("Using the direct Sodium 0.5.13 connected-glass backend with accurate transparency sorting");
        }
    }

    private static int index(Direction face) {
        return face == null ? UNCULLED_INDEX : face.ordinal();
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

        private boolean matches(
                SodiumCompatibleGlassModel model,
                BlockState state,
                BlockPos pos,
                long seed
        ) {
            return this.model == model
                    && this.state == state
                    && this.pos == pos.asLong()
                    && this.seed == seed;
        }

        private void rebuild(
                SodiumCompatibleGlassModel model,
                BlockAndTintGetter level,
                BlockState state,
                BlockPos pos,
                long seed
        ) {
            @SuppressWarnings("unchecked")
            List<BakedQuad>[] generated = (List<BakedQuad>[]) new List<?>[Direction.values().length + 1];

            for (int i = 0; i < generated.length; i++) {
                generated[i] = new ArrayList<>();
            }

            CapturingRenderContext context = new CapturingRenderContext();
            Supplier<RandomSource> randomSupplier = () -> RandomSource.create(seed);
            model.emitBlockQuads(level, state, pos, randomSupplier, context);
            Mesh mesh = context.finish();

            mesh.forEach(quad -> generated[index(quad.cullFace())].add(
                    quad.toBakedQuad(model.cleanAndClearGlass$spriteForTag(quad.tag()))
            ));

            for (int i = 0; i < generated.length; i++) {
                quads[i] = List.copyOf(generated[i]);
            }

            List<BakedQuad> flattened = new ArrayList<>();

            for (List<BakedQuad> faceQuads : generated) {
                flattened.addAll(faceQuads);
            }

            allQuads = List.copyOf(flattened);

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
        public QuadEmitter getEmitter() {
            return meshBuilder.getEmitter();
        }

        @Override
        public boolean hasTransform() {
            return false;
        }

        @Override
        public void pushTransform(QuadTransform transform) {
            throw new UnsupportedOperationException("C&CG's Sodium adapter does not use quad transforms");
        }

        @Override
        public void popTransform() {
            throw new UnsupportedOperationException("C&CG's Sodium adapter does not use quad transforms");
        }

        @Override
        @SuppressWarnings("removal")
        public BakedModelConsumer bakedModelConsumer() {
            return new BakedModelConsumer() {
                @Override
                public void accept(BakedModel model) {
                    throw new UnsupportedOperationException("C&CG's Sodium adapter only captures dynamic glass quads");
                }

                @Override
                public void accept(BakedModel model, BlockState state) {
                    throw new UnsupportedOperationException("C&CG's Sodium adapter only captures dynamic glass quads");
                }
            };
        }

        private Mesh finish() {
            return meshBuilder.build();
        }
    }
}
