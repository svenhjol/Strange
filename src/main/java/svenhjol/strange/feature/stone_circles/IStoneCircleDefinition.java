package svenhjol.strange.feature.stone_circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import svenhjol.strange.feature.runestones.RunestoneBlock;

import java.util.Optional;
import java.util.function.Supplier;

public interface IStoneCircleDefinition extends StringRepresentable {
    String name();

    TagKey<Block> pillarBlocks();

    Pair<Integer, Integer> pillarHeight();

    Pair<Integer, Integer> radius();

    Optional<Supplier<RunestoneBlock>> runestoneBlock();

    @Override
    default String getSerializedName() {
        return name();
    }
}
