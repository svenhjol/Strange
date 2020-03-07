package svenhjol.strange.scrolls.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class QuestsStorage implements Capability.IStorage<IQuestsCapability> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IQuestsCapability> capability, IQuestsCapability instance, Direction side) {
        return instance.writeNBT();
    }

    @Override
    public void readNBT(Capability<IQuestsCapability> capability, IQuestsCapability instance, Direction side, INBT nbt) {
        if (instance == null) return;
        CompoundNBT tag = null;

        if (nbt instanceof CompoundNBT) {
            tag = (CompoundNBT) nbt;
        }

        instance.readNBT(tag);
    }
}
