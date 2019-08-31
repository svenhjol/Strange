package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import svenhjol.strange.scrolls.module.Quests.QuestType;

import java.util.UUID;

public interface IQuest
{
    String getId();

    QuestType getType();

    ITextComponent getDescription();

    CompoundNBT getCriteria();

    UUID getSeller();

    int getTier();

    CompoundNBT toNBT();

    void fromNBT(CompoundNBT tag);

    void setId(String id);

    void setDescription(ITextComponent description);

    void setType(QuestType type);

    void setSeller(UUID sellerId);

    void setTier(int tier);

    void setCriteria(CompoundNBT criteria);
}
