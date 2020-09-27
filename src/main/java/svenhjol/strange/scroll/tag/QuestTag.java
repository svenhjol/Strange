package svenhjol.strange.scroll.tag;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.helper.ScrollHelper;

import java.util.UUID;

public class QuestTag implements ITag {
    private static final String ID_TAG = "id";
    private static final String TITLE_TAG = "title";
    private static final String MERCHANT_TAG = "merchant";
    private static final String REWARD_TAG = "reward";
    private static final String TIER_TAG = "tier";
    private static final String RARITY_TAG = "rarity";
    private static final String GATHER_TAG = "gather";
    private static final String HUNT_TAG = "hunt";

    private String id = "";
    private String title = "";
    private UUID merchant = ScrollHelper.ANY_MERCHANT;
    private int tier = 1;
    private int rarity = 0;
    private RewardTag rewardTag = new RewardTag(this);
    private GatherTag gatherTag = new GatherTag(this);
    private HuntTag huntTag = new HuntTag(this);

    public QuestTag() { }

    public QuestTag(UUID merchant, int rarity) {
        this.id = RandomStringUtils.randomAlphabetic(10);
        this.rarity = rarity;
        this.merchant = merchant;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putInt(RARITY_TAG, rarity);
        tag.putString(MERCHANT_TAG, merchant.toString());
        tag.putString(ID_TAG, id);
        tag.putString(TITLE_TAG, title);
        tag.put(REWARD_TAG, rewardTag.toTag());
        tag.put(GATHER_TAG, gatherTag.toTag());
        tag.put(HUNT_TAG, huntTag.toTag());

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        tier = tag.getInt(TIER_TAG);
        rarity = tag.getInt(RARITY_TAG);
        merchant = UUID.fromString(tag.getString(MERCHANT_TAG));
        id = tag.getString(ID_TAG);
        title = tag.getString(TITLE_TAG);
        rewardTag.fromTag(tag.getCompound(REWARD_TAG));
        gatherTag.fromTag(tag.getCompound(GATHER_TAG));
        huntTag.fromTag(tag.getCompound(HUNT_TAG));
    }

    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public int getRarity() {
        return rarity;
    }

    public String getTitle() {
        return title;
    }

    public UUID getMerchant() {
        return merchant;
    }

    public RewardTag getReward() {
        return rewardTag;
    }

    public GatherTag getGather() {
        return gatherTag;
    }

    public HuntTag getHunt() {
        return huntTag;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMerchant(UUID merchant) {
        this.merchant = merchant;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        gatherTag.complete(player, merchant);
        rewardTag.complete(player, merchant);
    }

    public boolean isSatisfied(PlayerEntity player) {
        update(player);
        return gatherTag.isSatisfied()
            && huntTag.isSatisfied();
    }

    public void update(PlayerEntity player) {
        gatherTag.update(player);
        huntTag.update(player);
    }
}
