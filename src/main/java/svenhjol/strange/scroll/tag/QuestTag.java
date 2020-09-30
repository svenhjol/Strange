package svenhjol.strange.scroll.tag;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.helper.ScrollHelper;
import svenhjol.strange.scroll.JsonDefinition;

import java.util.UUID;

public class QuestTag implements ITag {
    private static final String ID_TAG = "id";
    private static final String DEFINITION_TAG = "definition";
    private static final String TITLE_TAG = "title";
    private static final String MERCHANT_TAG = "merchant";
    private static final String REWARD_TAG = "reward";
    private static final String TIER_TAG = "tier";
    private static final String RARITY_TAG = "rarity";
    private static final String GATHER_TAG = "gather";
    private static final String HUNT_TAG = "hunt";
    private static final String EXPLORE_TAG = "explore";
    private static final String BOSS_TAG = "boss";

    private String id = "";
    private String definition = "";
    private String title = "";
    private UUID merchant = ScrollHelper.ANY_MERCHANT;
    private int tier = 1;
    private int rarity = 1;
    private boolean dirty = false;
    private RewardTag reward = new RewardTag(this);
    private GatherTag gather = new GatherTag(this);
    private HuntTag hunt = new HuntTag(this);
    private ExploreTag explore = new ExploreTag(this);
    private BossTag boss = new BossTag(this);

    public QuestTag() { }

    public QuestTag(JsonDefinition definition, UUID merchant, int rarity) {
        this.id = RandomStringUtils.randomAlphabetic(10);
        this.rarity = Math.max(1, rarity);
        this.tier = definition.getTier();
        this.definition = definition.getId();
        this.merchant = merchant;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putInt(RARITY_TAG, rarity);
        tag.putString(MERCHANT_TAG, merchant.toString());
        tag.putString(ID_TAG, id);
        tag.putString(DEFINITION_TAG, definition);
        tag.putString(TITLE_TAG, title);
        tag.put(REWARD_TAG, reward.toTag());
        tag.put(GATHER_TAG, gather.toTag());
        tag.put(HUNT_TAG, hunt.toTag());
        tag.put(EXPLORE_TAG, explore.toTag());
        tag.put(BOSS_TAG, boss.toTag());

        return tag;
    }

    public void fromTag(CompoundTag tag) {
        tier = tag.getInt(TIER_TAG);
        rarity = Math.max(1, tag.getInt(RARITY_TAG));
        merchant = UUID.fromString(tag.getString(MERCHANT_TAG));
        id = tag.getString(ID_TAG);
        definition = tag.getString(DEFINITION_TAG);
        title = tag.getString(TITLE_TAG);
        reward.fromTag(tag.getCompound(REWARD_TAG));
        gather.fromTag(tag.getCompound(GATHER_TAG));
        hunt.fromTag(tag.getCompound(HUNT_TAG));
        explore.fromTag(tag.getCompound(EXPLORE_TAG));
        boss.fromTag(tag.getCompound(BOSS_TAG));
    }

    public String getId() {
        return id;
    }

    public String getDefinition() {
        return definition;
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
        return reward;
    }

    public GatherTag getGather() {
        return gather;
    }

    public HuntTag getHunt() {
        return hunt;
    }

    public ExploreTag getExplore() {
        return explore;
    }

    public BossTag getBoss() {
        return boss;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void inventoryTick(PlayerEntity player) {
        explore.inventoryTick(player);
        boss.inventoryTick(player);
    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        gather.complete(player, merchant);
        explore.complete(player, merchant);
        reward.complete(player, merchant);
    }

    public boolean isSatisfied(PlayerEntity player) {
        update(player);
        return gather.isSatisfied()
            && hunt.isSatisfied()
            && explore.isSatisfied()
            && boss.isSatisfied();
    }

    public void update(PlayerEntity player) {
        gather.update(player);
        hunt.update(player);
        explore.update(player);
        boss.update(player);
    }

    public void markDirty(boolean flag) {
        this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }
}
