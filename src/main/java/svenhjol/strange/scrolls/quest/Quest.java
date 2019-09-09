package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.scrolls.module.Quests.QuestType;
import svenhjol.strange.scrolls.module.Scrollkeepers;
import svenhjol.strange.scrolls.quest.action.Action;

import java.util.UUID;

public class Quest implements IQuest
{
    private static final String SELLER = "seller";
    private static final String PURCHASED = "purchased";
    private static final String QUEST_ID = "questId";
    private static final String QUEST_TYPE = "questType";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String CRITERIA = "criteria";
    private static final String TIER = "tier";

    private String questId = "";
    private String seller = "";
    private String questType = "";
    private String title = "";
    private String description = "";
    private Criteria criteria = new Criteria(this);
    private long purchased;
    private int tier;

    public Quest()
    {
        this.setId(RandomStringUtils.randomAlphabetic(10));
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();

        tag.putString(QUEST_ID, questId);
        tag.putString(SELLER, seller);
        tag.putString(QUEST_TYPE, questType);
        tag.putString(DESCRIPTION, description);
        tag.put(CRITERIA, criteria.toNBT());
        tag.putLong(PURCHASED, purchased);
        tag.putInt(TIER, tier);

        return tag;
    }

    @Override
    public boolean respondTo(Event event)
    {
        boolean responded = false;

        for (Action action : this.criteria.getActions()) {
            responded = action.respondTo(event) || responded;
        }

        return responded;
    }

    @Override
    public void fromNBT(CompoundNBT tag)
    {
        questId = tag.getString(QUEST_ID);
        seller = tag.getString(SELLER);
        questType = tag.getString(QUEST_TYPE);
        title = tag.getString(TITLE);
        description = tag.getString(DESCRIPTION);
        purchased = tag.getLong(PURCHASED);
        tier = tag.getInt(TIER);

        criteria = new Criteria(this);
        criteria.fromNBT(tag.getCompound(CRITERIA));
    }

    @Override
    public void setId(String id)
    {
        this.questId = id;
    }

    @Override
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public void setType(QuestType type)
    {
        this.questType = type.toString();
    }

    @Override
    public void setSeller(UUID sellerId)
    {
        this.seller = sellerId.toString();
    }

    @Override
    public void setTier(int tier)
    {
        this.tier = tier;
    }

    @Override
    public void setCriteria(Criteria criteria)
    {
        this.criteria = criteria;
    }

    @Override
    public String getId()
    {
        return questId;
    }

    @Override
    public QuestType getType()
    {
        try {
            return QuestType.valueOf(questType);
        } catch (Exception e) {
            return QuestType.Gathering; // bad, the quest is broken
        }
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getTitle()
    {
        return this.title;
    }

    @Override
    public Criteria getCriteria()
    {
        return criteria;
    }

    @Override
    public UUID getSeller()
    {
        if (seller.isEmpty()) {
            return Scrollkeepers.ANY_SELLER; // bad, the quest is broken
        }
        return UUID.fromString(seller);
    }

    @Override
    public int getTier()
    {
        return tier;
    }
}
