package svenhjol.strange.feature.ebony_wood;

import svenhjol.charmony_api.iface.IConditionalAdvancement;
import svenhjol.charmony_api.iface.IConditionalAdvancementProvider;
import svenhjol.charmony_api.iface.IConditionalRecipe;
import svenhjol.charmony_api.iface.IConditionalRecipeProvider;
import svenhjol.strange.Strange;

import java.util.List;

public class EbonyWoodRecipeProvider implements IConditionalRecipeProvider, IConditionalAdvancementProvider {
    @Override
    public List<IConditionalRecipe> getRecipeConditions() {
        return List.of(
            new IConditionalRecipe() {
                @Override
                public boolean test() {
                    var enabled = Strange.isCharmFeatureEnabled("VariantWood");
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
                    var enabled = Strange.isCharmFeatureEnabled("Woodcutting");
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
                    var enabled = Strange.isCharmFeatureEnabled("VariantWood");
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
                    var enabled = Strange.isCharmFeatureEnabled("Woodcutting");
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
