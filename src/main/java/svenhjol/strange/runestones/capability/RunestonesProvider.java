package svenhjol.strange.runestones.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import svenhjol.strange.runestones.module.Runestones;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RunestonesProvider implements ICapabilityProvider {
    final IRunestonesCapability instance = new RunestonesCapability();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Runestones.RUNESTONES.orEmpty(cap, LazyOptional.of(() -> instance));
    }
}
