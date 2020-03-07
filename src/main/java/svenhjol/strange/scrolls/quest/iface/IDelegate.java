package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public interface IDelegate {
    String getId();

    String getType();

    boolean respondTo(Event event, @Nullable PlayerEntity player);

    boolean shouldRemove();

    boolean isSatisfied();

    boolean isCompletable();

    float getCompletion();

    CompoundNBT toNBT();

    void fromNBT(INBT nbt);

    void setQuest(IQuest quest);
}
