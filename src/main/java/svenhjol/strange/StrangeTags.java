package svenhjol.strange;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class StrangeTags {
    public static final TagKey<Block> COOKING_HEAT_SOURCES = TagKey.create(Registries.BLOCK,
        new ResourceLocation(Strange.ID, "cooking_heat_sources"));

    public static final TagKey<Biome> GROWS_EBONY_TREES = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "grows_ebony_trees"));

    public static final TagKey<Biome> NETHER_RUNESTONE_BIOMES = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "nether_runestone_biomes"));

    public static final TagKey<Structure> NETHER_RUNESTONE_STRUCTURES = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "nether_runestone_structures"));

    public static final TagKey<Biome> OVERWORLD_RUNESTONE_BIOMES = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "overworld_runestone_biomes"));

    public static final TagKey<Structure> OVERWORLD_RUNESTONE_STRUCTURES = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "overworld_runestone_structures"));

    public static final TagKey<Item> PIGLIN_BARTERS_FOR_BASTIONS = TagKey.create(Registries.ITEM,
        new ResourceLocation(Strange.ID, "piglin_barters_for_bastions"));

    public static final TagKey<Item> PIGLIN_BARTERS_FOR_DIRECTIONS = TagKey.create(Registries.ITEM,
        new ResourceLocation(Strange.ID, "piglin_barters_for_directions"));

    public static final TagKey<Item> PIGLIN_BARTERS_FOR_FORTRESSES = TagKey.create(Registries.ITEM,
        new ResourceLocation(Strange.ID, "piglin_barters_for_fortresses"));

    public static final TagKey<Structure> PIGLIN_BASTION_LOCATED = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "piglin_bastion_located"));

    public static final TagKey<Structure> PIGLIN_FORTRESS_LOCATED = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "piglin_fortress_located"));
}
