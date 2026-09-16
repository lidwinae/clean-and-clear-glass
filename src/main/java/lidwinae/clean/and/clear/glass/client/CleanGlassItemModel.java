package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CleanGlassItemModel implements ItemModel {
    private enum Kind {
        FIXED,
        TINTED
    }

    private final ItemModel vanilla;
    private final ItemModel first;
    private final ItemModel second;
    private final ItemModel third;
    private final Kind kind;

    private CleanGlassItemModel(
            ItemModel vanilla,
            ItemModel first,
            ItemModel second,
            ItemModel third,
            Kind kind
    ) {
        this.vanilla = vanilla;
        this.first = first;
        this.second = second;
        this.third = third;
        this.kind = kind;
    }

    public static CleanGlassItemModel fixed(
            ItemModel vanilla,
            ItemModel clean
    ) {
        return new CleanGlassItemModel(
                vanilla,
                clean,
                clean,
                clean,
                Kind.FIXED
        );
    }

    public static CleanGlassItemModel tinted(
            ItemModel vanilla,
            ItemModel subtle,
            ItemModel visible,
            ItemModel clear
    ) {
        return new CleanGlassItemModel(
                vanilla,
                subtle,
                visible,
                clear,
                Kind.TINTED
        );
    }

    @Override
    public void update(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed
    ) {
        ItemModel selected = vanilla;

        if (CleanAndClearGlassConfig.enabled()) {
            selected = switch (kind) {
                case FIXED -> first;

                case TINTED -> switch (
                        CleanAndClearGlassConfig.tintedGlassStyle()
                        ) {
                    case SUBTLE -> first;
                    case VISIBLE -> second;
                    case CLEAR -> third;
                };
            };
        }

        selected.update(
                renderState,
                stack,
                resolver,
                displayContext,
                level,
                owner,
                seed
        );
    }
}
