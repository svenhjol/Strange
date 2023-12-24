package svenhjol.strange.feature.ebony_wood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.feature.custom_wood.CustomWood;
import svenhjol.charmony.feature.variant_wood.VariantWood;
import svenhjol.charmony.api.CharmonyApi;
import svenhjol.charmony.api.iface.IVariantWoodMaterial;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;

import java.util.function.Supplier;

public class EbonyWood extends CommonFeature {
    static Supplier<BlockSetType> blockSetType;
    static Supplier<WoodType> woodType;
    static IVariantWoodMaterial material;
    static ResourceKey<ConfiguredFeature<?, ?>> TREE_FEATURE;
    static ResourceKey<ConfiguredFeature<?, ?>> TREES_FEATURE;
    static ResourceKey<PlacedFeature> TREE_PLACEMENT;
    static ResourceKey<PlacedFeature> TREES_PLACEMENT;

    @Override
    public String description() {
        return "Ebony is a dark wood obtainable from ebony trees that grow in savanna biomes.";
    }

    @Override
    public void register() {
        material = EbonyMaterial.EBONY;
        blockSetType = mod().registry().blockSetType(material);
        woodType = mod().registry().woodType(material.getSerializedName(), material);

        TREE_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Strange.ID, "ebony_tree"));
        TREES_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Strange.ID, "ebony_trees"));

        TREE_PLACEMENT = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Strange.ID, "ebony_tree"));
        TREES_PLACEMENT = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Strange.ID, "ebony_trees"));

        CustomWood.registerWood(mod().registry(), new EbonyWoodDefinition());
        VariantWood.registerWood(mod().registry(), material);

        CharmonyApi.registerProvider(this);
        CharmonyApi.registerProvider(new EbonyWoodDataProvider());
    }

    @Override
    public void runWhenEnabled() {
        mod().registry().biomeAddition("ebony_trees",
            holder -> holder.is(StrangeTags.GROWS_EBONY_TREES), GenerationStep.Decoration.VEGETAL_DECORATION, TREES_PLACEMENT);
    }
}
