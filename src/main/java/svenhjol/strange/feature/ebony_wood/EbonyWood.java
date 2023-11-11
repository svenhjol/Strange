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
import svenhjol.charmony_api.CharmonyApi;
import svenhjol.charmony_api.iface.*;
import svenhjol.strange.Strange;
import svenhjol.strange.StrangeTags;

import java.util.List;
import java.util.function.Supplier;

public class EbonyWood extends CommonFeature implements
    IVariantBarrelProvider,
    IVariantBookshelfProvider,
    IVariantChestProvider,
    IVariantChestBoatProvider,
    IVariantChiseledBookshelfProvider,
    IVariantLadderProvider
{
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

        CustomWood.registerWood(this, mod().registry(), new EbonyWoodDefinition());

        CharmonyApi.registerProvider(this);
        CharmonyApi.registerProvider(new EbonyWoodRecipeProvider());
    }

    @Override
    public void runWhenEnabled() {
        mod().registry().biomeAddition("ebony_trees",
            holder -> holder.is(StrangeTags.GROWS_EBONY_TREES), GenerationStep.Decoration.VEGETAL_DECORATION, TREES_PLACEMENT);
    }

    @Override
    public List<IVariantMaterial> getVariantBarrels() {
        return List.of(material);
    }

    @Override
    public List<IVariantMaterial> getVariantBookshelves() {
        return List.of(material);
    }

    @Override
    public List<IVariantMaterial> getVariantChests() {
        return List.of(material);
    }

    @Override
    public List<IVariantMaterial> getVariantChiseledBookshelves() {
        return List.of(material);
    }

    @Override
    public List<IVariantMaterial> getVariantLadders() {
        return List.of(material);
    }

    @Override
    public List<IVariantChestBoatDefinition> getVariantChestBoatDefinitions() {
        return List.of(new EbonyChestBoatDefinition());
    }
}
