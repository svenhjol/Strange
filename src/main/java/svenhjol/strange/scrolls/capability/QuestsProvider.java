package svenhjol.strange.scrolls.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import svenhjol.strange.scrolls.module.Quests;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QuestsProvider implements ICapabilityProvider
{
    IQuestsCapability instance = new QuestsCapability();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        return Quests.QUESTS.orEmpty(cap, LazyOptional.of(() -> instance));
    }
}
