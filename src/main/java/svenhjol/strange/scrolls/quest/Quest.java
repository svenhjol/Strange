package svenhjol.strange.scrolls.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.scrolls.module.Scrollkeepers;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Quest implements IQuest
{
    private static final String SELLER = "seller";
    private static final String PURCHASED = "purchased";
    private static final String QUEST_ID = "questId";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String STATE = "state";
    private static final String CRITERIA = "criteria";
    private static final String TIER = "tier";
    private static final String VALUE = "value";

    private String questId = "";
    private String seller = "";
    private String title = "";
    private String description = "";
    private Criteria criteria = new Criteria(this);
    private State state = State.NotStarted;
    private long purchased;
    private int tier;
    private float value;

    public Quest()
    {
        this.generateId();
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();

        tag.putInt(TIER, tier);
        tag.putString(QUEST_ID, questId);
        tag.putString(SELLER, seller);
        tag.putString(TITLE, title);
        tag.putString(DESCRIPTION, description);
        tag.putString(STATE, state.toString());
        tag.putLong(PURCHASED, purchased);
        tag.putFloat(VALUE, value);
        tag.put(CRITERIA, criteria.toNBT());

        return tag;
    }

    @Override
    public void fromNBT(CompoundNBT tag)
    {
        questId = tag.getString(QUEST_ID);
        seller = tag.getString(SELLER);
        title = tag.getString(TITLE);
        description = tag.getString(DESCRIPTION);
        purchased = tag.getLong(PURCHASED);
        state = State.valueOrDefault(tag.getString(STATE), State.NotStarted);
        tier = tag.getInt(TIER);
        value = tag.getFloat(VALUE);

        criteria = new Criteria(this);
        criteria.fromNBT(tag.getCompound(CRITERIA));
    }

    @Override
    public void generateId()
    {
        this.setId(RandomStringUtils.randomAlphabetic(10));
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        boolean responded = false;

        final List<Condition> conditions = this.criteria.getConditions();

        for (Condition condition : conditions) {
            responded = condition.respondTo(event, player) || responded;
        }

        return responded;
    }

    @Override
    public State getState()
    {
        return state;
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
    public void setState(State state)
    {
        this.state = state;
    }

    @Override
    public void setValue(float value)
    {
        this.value = value;
    }

    @Override
    public String getId()
    {
        return questId;
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
    public float getValue()
    {
        return this.value;
    }

    @Override
    public Criteria getCriteria()
    {
        return criteria;
    }

    @Override
    public UUID getSeller()
    {
        if (seller.isEmpty()) return Scrollkeepers.ANY_SELLER;
        return UUID.fromString(seller);
    }

    @Override
    public int getTier()
    {
        return tier;
    }
}
