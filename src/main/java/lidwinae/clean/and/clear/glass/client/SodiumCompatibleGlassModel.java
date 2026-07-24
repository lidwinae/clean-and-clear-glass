package lidwinae.clean.and.clear.glass.client;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SodiumCompatibleGlassModel extends FabricBakedModel {
    TextureAtlasSprite cleanAndClearGlass$spriteForTag(int tag);

    boolean cleanAndClearGlass$requiresAccurateTransparency();
}
