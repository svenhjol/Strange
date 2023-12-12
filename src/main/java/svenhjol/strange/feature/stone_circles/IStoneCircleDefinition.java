package svenhjol.strange.feature.stone_circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import svenhjol.strange.feature.runestones.RunestoneBlock;

import java.util.Optional;
import java.util.function.Supplier;

public interface IStoneCircleDefinition extends StringRepresentable {
    String name();

    TagKey<Block> pillarBlocks();

    Optional<Supplier<RunestoneBlock>> runestoneBlock();

    default Pair<Integer, Integer> pillarHeight() {
        return Pair.of(4, 8);
    }

    default Pair<Integer, Integer> radius() {
        return Pair.of(6, 15);
    }

    default int maxRunestones() {
        return 6;
    }

    default double runestoneChance() {
        return 0.5d;
    }

    default BlockPos ceilingReposition(WorldGenLevel level, BlockPos pos) {
        return new BlockPos(pos.getX(), level.getMinBuildHeight() + 15, pos.getZ());
    }

    default int jaggednessTolerance() {
        return 20;
    }

    @Override
    default String getSerializedName() {
        return name();
    }
}
