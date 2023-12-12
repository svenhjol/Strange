package svenhjol.strange;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

public class StrangeTags {
    public static final TagKey<Block> BLACKSTONE_PILLAR_BLOCKS = TagKey.create(Registries.BLOCK,
        new ResourceLocation(Strange.ID, "blackstone_pillar_blocks"));

    public static final TagKey<Biome> BLACKSTONE_RUNESTONE_BIOME_LOCATED = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "blackstone_runestone_biome_located"));

    public static final TagKey<Structure> BLACKSTONE_RUNESTONE_STRUCTURE_LOCATED = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "blackstone_runestone_structure_located"));

    public static final TagKey<Block> COOKING_HEAT_SOURCES = TagKey.create(Registries.BLOCK,
        new ResourceLocation(Strange.ID, "cooking_heat_sources"));

    public static final TagKey<Biome> GROWS_EBONY_TREES = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "grows_ebony_trees"));

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

    public static final TagKey<Block> STONE_PILLAR_BLOCKS = TagKey.create(Registries.BLOCK,
        new ResourceLocation(Strange.ID, "stone_pillar_blocks"));

    public static final TagKey<Biome> STONE_RUNESTONE_BIOME_LOCATED = TagKey.create(Registries.BIOME,
        new ResourceLocation(Strange.ID, "stone_runestone_biome_located"));

    public static final TagKey<Structure> STONE_RUNESTONE_STRUCTURE_LOCATED = TagKey.create(Registries.STRUCTURE,
        new ResourceLocation(Strange.ID, "stone_runestone_structure_located"));

}
