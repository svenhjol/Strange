package svenhjol.strange.feature.ebony_wood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.feature.variant_barrels.VariantBarrels;
import svenhjol.charm.feature.variant_chests.VariantChests;
import svenhjol.charm.feature.variant_ladders.VariantLadders;
import svenhjol.charm.feature.wood.Wood;
import svenhjol.charm.feature.woodcutters.Woodcutters;
import svenhjol.charm_api.iface.IProvidesWandererTrades;
import svenhjol.charm_api.iface.IRemovesRecipes;
import svenhjol.charm_api.iface.IWandererTrade;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.base.block.*;
import svenhjol.charm_core.init.CharmApi;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Feature(mod = Strange.MOD_ID, description = "A dark grey wood. " +
    "Ebony trees can be found in savanna biomes.")
public class EbonyWood extends CharmFeature implements IProvidesWandererTrades, IRemovesRecipes {
    static Supplier<WoodType> WOOD_TYPE;
    static Supplier<CharmDoorBlock> DOOR_BLOCK;
    static Supplier<CharmTrapdoorBlock> TRAPDOOR_BLOCK;
    static Supplier<CharmLeavesBlock> LEAVES_BLOCK;
    static Supplier<CharmLogBlock> LOG_BLOCK;
    static Supplier<CharmSaplingBlock> SAPLING_BLOCK;
    static ResourceKey<ConfiguredFeature<?, ?>> TREE_FEATURE;
    static ResourceKey<ConfiguredFeature<?, ?>> TREES_FEATURE;
    static ResourceKey<PlacedFeature> TREE_PLACEMENT;
    static ResourceKey<PlacedFeature> TREES_PLACEMENT;
    public static TagKey<Biome> GROWS_EBONY_TREES = TagKey.create(Registries.BIOME, Strange.makeId("grows_ebony_trees"));

    @Override
    public void register() {
        var material = EbonyMaterial.EBONY;
        var registry = Strange.REGISTRY;

        WOOD_TYPE = Wood.registerWoodType(registry, material);
        DOOR_BLOCK = Wood.registerDoor(registry, this, material).getFirst();
        TRAPDOOR_BLOCK = Wood.registerTrapdoor(registry, this, material).getFirst();
        LEAVES_BLOCK = Wood.registerLeaves(registry, this, material).getFirst();

        var log = Wood.registerLog(registry, this, material);
        LOG_BLOCK = log.get("ebony_log").getFirst(); // Need reference to add to tree feature.

        Wood.registerBoat(registry, this, material);
        Wood.registerButton(registry, this, material);
        Wood.registerFence(registry, this, material);
        Wood.registerGate(registry, this, material);
        Wood.registerPlanksSlabsAndStairs(registry, this, material);
        Wood.registerPressurePlate(registry, this, material);
        Wood.registerSign(registry, this, material);

        Wood.registerBarrel(registry, material);
        Wood.registerBookshelf(registry, material);
        Wood.registerChest(registry, material);
        Wood.registerTrappedChest(registry, material);
        Wood.registerLadder(registry, material);

        SAPLING_BLOCK = Wood.registerSapling(registry, this, material).getFirst();

        TREE_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, Strange.makeId("ebony_tree"));
        TREES_FEATURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, Strange.makeId("ebony_trees"));

        TREE_PLACEMENT = ResourceKey.create(Registries.PLACED_FEATURE, Strange.makeId("ebony_tree"));
        TREES_PLACEMENT = ResourceKey.create(Registries.PLACED_FEATURE, Strange.makeId("ebony_trees"));

        CharmApi.registerProvider(this);
    }

    @Override
    public void runWhenEnabled() {
        Strange.REGISTRY.biomeAddition("ebony_trees", holder -> holder.is(GROWS_EBONY_TREES),
            GenerationStep.Decoration.VEGETAL_DECORATION, TREES_PLACEMENT);
    }

    @Override
    public List<IWandererTrade> getWandererTrades() {
        return List.of();
    }

    @Override
    public List<IWandererTrade> getRareWandererTrades() {
        return List.of(new IWandererTrade() {
            @Override
            public ItemLike getItem() {
                return SAPLING_BLOCK.get();
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public int getCost() {
                return 20;
            }
        });
    }

    @Override
    public List<ResourceLocation> getRecipesToRemove() {
        List<ResourceLocation> remove = new ArrayList<>();

        if (!Charm.LOADER.isEnabled(VariantBarrels.class)) {
            remove.add(Strange.makeId("ebony_barrel"));
        }

        if (!Charm.LOADER.isEnabled(VariantChests.class)) {
            remove.add(Strange.makeId("ebony_chest"));
            remove.add(Strange.makeId("ebony_trapped_chest"));
        }

        if (!Charm.LOADER.isEnabled(VariantLadders.class)) {
            remove.add(Strange.makeId("ebony_ladder"));
        }

        if (!Charm.LOADER.isEnabled(Woodcutters.class)) {
            remove.add(Strange.makeId("ebony_wood/woodcutting/"));
        }

        return remove;
    }
}
