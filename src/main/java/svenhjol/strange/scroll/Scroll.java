package svenhjol.strange.scroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.helper.ScrollHelper;

import java.util.UUID;

public class Scroll implements IScrollSerializable {
    private static final String ID_TAG = "id";
    private static final String TITLE_TAG = "title";
    private static final String DESCRIPTION_TAG = "description";
    private static final String MERCHANT_TAG = "merchant";
    private static final String REWARD_TAG = "reward";
    private static final String TIER_TAG = "tier";
    private static final String RARITY_TAG = "rarity";

    private String id = "";
    private String merchant = "";
    private String title = "";
    private String description = "";
    private int tier = 1;
    private int rarity = 0;
    private Reward reward = new Reward(this);

    public Scroll(UUID merchant, int rarity) {
        this.id = RandomStringUtils.randomAlphabetic(10);
        this.merchant = merchant.toString();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putInt(RARITY_TAG, rarity);
        tag.putString(MERCHANT_TAG, merchant);
        tag.putString(ID_TAG, id);
        tag.putString(TITLE_TAG, title);
        tag.putString(DESCRIPTION_TAG, description);
        tag.put(REWARD_TAG, reward.toTag());

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        tier = tag.getInt(TIER_TAG);
        rarity = tag.getInt(RARITY_TAG);
        merchant = tag.getString(MERCHANT_TAG);
        id = tag.getString(ID_TAG);
        title = tag.getString(TITLE_TAG);
        description = tag.getString(DESCRIPTION_TAG);
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

    public String getDescription() {
        return description;
    }

    public UUID getMerchant() {
        if (merchant.isEmpty())
            return ScrollHelper.ANY_MERCHANT;

        return UUID.fromString(merchant);
    }

    public Reward getReward() {
        return reward;
    }

    public void complete(PlayerEntity player) {
        reward.complete(player);
    }
}
