package svenhjol.strange.runestones.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public interface IRunestonesCapability
{
    void discoverType(int rune);

    void recordDestination(BlockPos runePos, BlockPos destPos);

    List<Integer> getDiscoveredTypes();

    @Nullable
    BlockPos getDestination(BlockPos runePos);

    void readNBT(INBT tag);

    INBT writeNBT();
}
