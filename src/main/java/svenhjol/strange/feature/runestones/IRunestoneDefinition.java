package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface IRunestoneDefinition {
    Supplier<RunestoneBlock> block();

    BiFunction<Level, BlockPos, Optional<TagKey<?>>> getDestination();

    default Supplier<ItemLike> activationItem() {
        return () -> Items.ENDER_EYE;
    }
}
