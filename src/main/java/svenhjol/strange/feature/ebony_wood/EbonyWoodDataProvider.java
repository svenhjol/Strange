package svenhjol.strange.feature.ebony_wood;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.api.iface.IConditionalAdvancement;
import svenhjol.charmony.api.iface.IConditionalAdvancementProvider;
import svenhjol.charmony.api.iface.IConditionalRecipe;
import svenhjol.charmony.api.iface.IConditionalRecipeProvider;
import svenhjol.strange.Strange;

import java.util.List;

public class EbonyWoodDataProvider implements IConditionalRecipeProvider, IConditionalAdvancementProvider {
    static final ResourceLocation VARIANT_WOOD = new ResourceLocation(Strange.CHARMONY_ID, "variant_wood");
    static final ResourceLocation WOODCUTTING = new ResourceLocation(Strange.CHARM_ID, "woodcutting");

    @Override
    public List<IConditionalRecipe> getRecipeConditions() {
        return List.of(
            new IConditionalRecipe() {
                @Override
                public boolean test() {
                    var enabled = Strange.isFeatureEnabled(VARIANT_WOOD);
                    return enabled;
                }

                @Override
                public List<String> recipes() {
                    return List.of(
                        "ebony_wood/*barrel",
                        "ebony_wood/*bookshelf",
                        "ebony_wood/*chest",
                        "ebony_wood/*ladder"
                    );
                }
            },
            new IConditionalRecipe() {
                @Override
                public boolean test() {
                    var enabled = Strange.isFeatureEnabled(WOODCUTTING);
                    return enabled;
                }

                @Override
                public List<String> recipes() {
                    return List.of("ebony_wood/woodcutting/*");
                }
            }
        );
    }

    @Override
    public List<IConditionalAdvancement> getAdvancementConditions() {
        return List.of(
            new IConditionalAdvancement() {
                @Override
                public boolean test() {
                    var enabled = Strange.isFeatureEnabled(VARIANT_WOOD);
                    return enabled;
                }

                @Override
                public List<String> advancements() {
                    return List.of(
                        "ebony_wood/*barrel",
                        "ebony_wood/*bookshelf",
                        "ebony_wood/*chest",
                        "ebony_wood/*ladder"
                    );
                }
            },
            new IConditionalAdvancement() {
                @Override
                public boolean test() {
                    var enabled = Strange.isFeatureEnabled(WOODCUTTING);
                    return enabled;
                }

                @Override
                public List<String> advancements() {
                    return List.of("ebony_wood/woodcutting/*");
                }
            }
        );
    }
}
