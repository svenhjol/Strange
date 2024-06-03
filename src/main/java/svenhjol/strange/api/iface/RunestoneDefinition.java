package svenhjol.strange.api.iface;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import svenhjol.strange.api.impl.RunestoneLocation;

import java.util.Optional;
import java.util.function.Supplier;

public interface RunestoneDefinition {
    Supplier<? extends Block> block();

    Optional<RunestoneLocation> location(LevelAccessor level, BlockPos pos, RandomSource random);

    Optional<TagKey<Item>> items();

    Optional<TagKey<Biome>> biomes();

    Optional<TagKey<Structure>> structures();
}