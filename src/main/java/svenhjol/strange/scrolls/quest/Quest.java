package svenhjol.strange.scrolls.quest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.scrolls.module.Quests.QuestType;
import svenhjol.strange.scrolls.module.Scrollkeepers;

import java.util.UUID;

public class Quest implements IQuest
{
    public static final String SELLER = "seller";
    public static final String PURCHASED = "purchased";
    public static final String QUEST_ID = "questId";
    public static final String QUEST_TYPE = "questType";
    public static final String DESCRIPTION = "description";
    public static final String CRITERIA = "criteria";
    public static final String TIER = "tier";

    private String questId = "";
    private String seller = "";
    private String questType = "";
    private String description = "";
    private CompoundNBT criteria = new CompoundNBT();
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
//        String criteria = "";
//
//        try {
//            final ByteArrayOutputStream out = new ByteArrayOutputStream();
//            CompressedStreamTools.writeCompressed( this.criteria, out );
//            criteria = javax.xml.bind.DatatypeConverter.printBase64Binary( out.toByteArray() );
//        } catch (Exception e) {
//            Meson.log("toNBT serialization failed", e);
//        }

        tag.putString(QUEST_ID, questId);
        tag.putString(SELLER, seller);
        tag.putString(QUEST_TYPE, questType);
        tag.putString(DESCRIPTION, description);
        tag.put(CRITERIA, criteria);
        tag.putLong(PURCHASED, purchased);
        tag.putInt(TIER, tier);

        return tag;
    }

    @Override
    public void fromNBT(CompoundNBT tag)
    {
        questId = tag.getString(QUEST_ID);
        seller = tag.getString(SELLER);
        questType = tag.getString(QUEST_TYPE);
        description = tag.getString(DESCRIPTION);
        purchased = tag.getLong(PURCHASED);
        criteria = tag.getCompound(CRITERIA);
        tier = tag.getInt(TIER);

//        try {
//            final byte[] byteData = javax.xml.bind.DatatypeConverter.parseBase64Binary( tag.getString(CRITERIA) );
//            this.criteria = CompressedStreamTools.readCompressed( new ByteArrayInputStream( byteData ) );
//        } catch (Exception e) {
//            Meson.log("fromNBT unserialization failed", e);
//        }
    }

    @Override
    public void setId(String id)
    {
        this.questId = id;
    }

    @Override
    public void setDescription(ITextComponent description)
    {
        this.description = description.toString();
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
    public void setCriteria(CompoundNBT criteria)
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
            // bad, the quest is broken
            return QuestType.Gathering;
        }
    }

    @Override
    public ITextComponent getDescription()
    {
        return new StringTextComponent(description);
    }

    @Override
    public CompoundNBT getCriteria()
    {
        return criteria;
    }

    @Override
    public UUID getSeller()
    {
        if (seller.isEmpty()) {
            // bad, the quest is broken
            return Scrollkeepers.ANY_SELLER;
        }
        return UUID.fromString(seller);
    }

    @Override
    public int getTier()
    {
        return tier;
    }
}
