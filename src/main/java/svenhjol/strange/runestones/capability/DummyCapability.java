package svenhjol.strange.runestones.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public class DummyCapability implements IRunestonesCapability {
    @Override
    public void discoverType(int rune) {

    }

    @Override
    public void recordDestination(BlockPos runePos, BlockPos destPos) {

    }

    @Override
    public List<Integer> getDiscoveredTypes() {
        return null;
    }

    @Nullable
    @Override
    public BlockPos getDestination(BlockPos runePos) {
        return null;
    }

    @Override
    public void readNBT(INBT tag) {
        // no op
    }

    @Override
    public INBT writeNBT() {
        return null;
    }
}
