package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.util.Objects;

final class SodiumModelSupport {
    private static final boolean SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");
    private static final boolean INDIUM_LOADED = FabricLoader.getInstance().isModLoaded("indium");
    private static final boolean SUPPORTED_SODIUM_LOADED = FabricLoader.getInstance()
            .getModContainer("sodium")
            .map(container -> container.getMetadata().getVersion().getFriendlyString().startsWith("0.2.0"))
            .orElse(false);
    private static final RenderMaterial INDIGO_NO_DIFFUSE_MATERIAL = IndigoRenderer.INSTANCE
            .materialFinder()
            .disableDiffuse(0, true)
            .find();
    private static volatile MaterialCache alternateMaterialCache;

    private SodiumModelSupport() {
    }

    static boolean isSodiumLoaded() {
        return SODIUM_LOADED;
    }

    static boolean isSupportedSodiumLoaded() {
        return SUPPORTED_SODIUM_LOADED;
    }

    static RenderMaterial noDiffuseMaterial(QuadEmitter emitter) {
        RenderMaterial emitterMaterial = emitter.material();

        if (isCompatible(emitterMaterial, INDIGO_NO_DIFFUSE_MATERIAL)) {
            return INDIGO_NO_DIFFUSE_MATERIAL;
        }

        MaterialCache cached = alternateMaterialCache;

        if (cached != null && cached.matches(emitterMaterial)) {
            return cached.material;
        }

        RenderMaterial material = createCompatibleNoDiffuseMaterial(emitterMaterial);

        if (material != emitterMaterial) {
            alternateMaterialCache = new MaterialCache(emitterMaterial.getClass(), material);
        }

        return material;
    }

    private static RenderMaterial createCompatibleNoDiffuseMaterial(RenderMaterial emitterMaterial) {
        RenderMaterial material = createNoDiffuseMaterial(RendererAccess.INSTANCE.getRenderer());

        if (isCompatible(emitterMaterial, material)) {
            return material;
        }

        // Minecraft 1.16.5 can use both C&CG's Indigo-backed Sodium capture
        // context and an Indium tessellation context in the same session.
        Renderer indiumRenderer = findIndiumRenderer();
        material = createNoDiffuseMaterial(indiumRenderer);

        return isCompatible(emitterMaterial, material) ? material : emitterMaterial;
    }

    private static RenderMaterial createNoDiffuseMaterial(Renderer renderer) {
        return renderer == null
                ? null
                : renderer.materialFinder().disableDiffuse(0, true).find();
    }

    private static Renderer findIndiumRenderer() {
        if (!INDIUM_LOADED) {
            return null;
        }

        try {
            Class<?> rendererClass = Class.forName("link.infra.indium.renderer.IndiumRenderer");
            Field instanceField = rendererClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            return instance instanceof Renderer ? (Renderer) instance : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static boolean isCompatible(RenderMaterial emitterMaterial, RenderMaterial candidate) {
        return emitterMaterial != null
                && candidate != null
                && emitterMaterial.getClass().isInstance(candidate);
    }

    private static final class MaterialCache {
        private final Class<?> materialClass;
        private final RenderMaterial material;

        private MaterialCache(Class<?> materialClass, RenderMaterial material) {
            this.materialClass = Objects.requireNonNull(materialClass);
            this.material = Objects.requireNonNull(material);
        }

        private boolean matches(RenderMaterial emitterMaterial) {
            return emitterMaterial != null && materialClass == emitterMaterial.getClass();
        }
    }
}
