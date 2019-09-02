package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.scrolls.module.Quests.QuestType;

import java.util.UUID;

public interface IQuest
{
    String getId();

    QuestType getType();

    ITextComponent getDescription();

    Criteria getCriteria();

    UUID getSeller();

    int getTier();

    CompoundNBT toNBT();

    boolean respondTo(Event event);

    void fromNBT(CompoundNBT tag);

    void setId(String id);

    void setDescription(ITextComponent description);

    void setType(QuestType type);

    void setSeller(UUID sellerId);

    void setTier(int tier);

    void setCriteria(Criteria criteria);
}
