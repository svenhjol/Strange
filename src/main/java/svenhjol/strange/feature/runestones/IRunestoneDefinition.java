package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;

import java.util.Optional;
import java.util.function.Supplier;

public interface IRunestoneDefinition {
    Supplier<RunestoneBlock> block();

    Optional<Location> getLocation(LevelAccessor level, BlockPos pos, RandomSource random);

    default Supplier<ItemLike> activationItem() {
        return () -> Items.ENDER_EYE;
    }
}
