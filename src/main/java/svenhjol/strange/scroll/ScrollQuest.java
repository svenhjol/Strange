package svenhjol.strange.scroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.helper.ScrollHelper;

import java.util.UUID;

public class ScrollQuest implements IScrollSerializable {
    private static final String ID_TAG = "id";
    private static final String TITLE_TAG = "title";
    private static final String MERCHANT_TAG = "merchant";
    private static final String REWARD_TAG = "reward";
    private static final String TIER_TAG = "tier";
    private static final String RARITY_TAG = "rarity";

    private String id = "";
    private UUID merchant = ScrollHelper.ANY_MERCHANT;
    private String title = "";
    private int tier = 1;
    private int rarity = 0;
    private Reward reward = new Reward(this);

    public ScrollQuest() { }

    public ScrollQuest(UUID merchant, int rarity) {
        this.id = RandomStringUtils.randomAlphabetic(10);
        this.merchant = merchant;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putInt(RARITY_TAG, rarity);
        tag.putString(MERCHANT_TAG, merchant.toString());
        tag.putString(ID_TAG, id);
        tag.putString(TITLE_TAG, title);
        tag.put(REWARD_TAG, reward.toTag());

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        tier = tag.getInt(TIER_TAG);
        rarity = tag.getInt(RARITY_TAG);
        merchant = UUID.fromString(tag.getString(MERCHANT_TAG));
        id = tag.getString(ID_TAG);
        title = tag.getString(TITLE_TAG);
        reward.fromTag(tag.getCompound(REWARD_TAG));
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

    public Reward getReward() {
        return reward;
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

    public void complete(PlayerEntity player) {
        reward.complete(player);
    }
}
