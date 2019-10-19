package svenhjol.strange.runestones.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;

public interface IRunestonesCapability
{
    void discoverType(int rune);

    void recordDestination(BlockPos runePos, BlockPos destPos);

    void readNBT(INBT tag);

    INBT writeNBT();
}
