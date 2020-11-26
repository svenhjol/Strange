package svenhjol.strange.scrolls.tag;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.scrolls.ScrollHelper;
import svenhjol.strange.scrolls.JsonDefinition;

import java.util.UUID;

public class Quest implements ISerializable {
    private static final String ID_TAG = "id";
    private static final String DEFINITION_TAG = "definition";
    private static final String TITLE_TAG = "title";
    private static final String DESCRIPTION_TAG = "description";
    private static final String HINT_TAG = "hint";
    private static final String MERCHANT_TAG = "merchant";
    private static final String OWNER_TAG = "owner";
    private static final String REWARD_TAG = "reward";
    private static final String TIER_TAG = "tier";
    private static final String RARITY_TAG = "rarity";
    private static final String TIME_TAG = "time";
    private static final String EXPIRY_TAG = "expiry";
    private static final String GATHER_TAG = "gather";
    private static final String HUNT_TAG = "hunt";
    private static final String EXPLORE_TAG = "explore";
    private static final String BOSS_TAG = "boss";

    private String id = "";
    private String definition = "";
    private String title = "";
    private String description = "";
    private String hint = "";
    private UUID owner = ScrollHelper.ANY_UUID;
    private UUID merchant = ScrollHelper.ANY_UUID;
    private int tier = 1;
    private int rarity = 1;
    private int expiry = 0;
    private int time = 0;
    private boolean dirty = false;
    private Reward reward = new Reward(this);
    private Gather gather = new Gather(this);
    private Hunt hunt = new Hunt(this);
    private Explore explore = new Explore(this);
    private Boss boss = new Boss(this);

    private Quest() { }

    public Quest(JsonDefinition definition, UUID owner, UUID merchant, int rarity, int currentTime) {
        this.id = RandomStringUtils.randomAlphabetic(4).toLowerCase();
        this.rarity = Math.max(1, rarity);
        this.tier = definition.getTier();
        this.definition = definition.getId();
        this.time = currentTime;
        this.expiry = currentTime + definition.getTimeLimit();
        this.merchant = merchant;
        this.owner = owner;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putInt(RARITY_TAG, rarity);
        tag.putInt(EXPIRY_TAG, expiry);
        tag.putInt(TIME_TAG, time);
        tag.putString(MERCHANT_TAG, merchant.toString());
        tag.putString(OWNER_TAG, owner.toString());
        tag.putString(ID_TAG, id);
        tag.putString(DEFINITION_TAG, definition);
        tag.putString(TITLE_TAG, title);
        tag.putString(DESCRIPTION_TAG, description);
        tag.putString(HINT_TAG, hint);
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
        expiry = Math.max(0, tag.getInt(EXPIRY_TAG));
        time = Math.max(0, tag.getInt(TIME_TAG));
        merchant = UUID.fromString(tag.getString(MERCHANT_TAG));
        owner = UUID.fromString(tag.getString(OWNER_TAG));
        id = tag.getString(ID_TAG);
        definition = tag.getString(DEFINITION_TAG);
        title = tag.getString(TITLE_TAG);
        description = tag.getString(DESCRIPTION_TAG);
        hint = tag.getString(HINT_TAG);
        reward.fromTag(tag.getCompound(REWARD_TAG));
        gather.fromTag(tag.getCompound(GATHER_TAG));
        hunt.fromTag(tag.getCompound(HUNT_TAG));
        explore.fromTag(tag.getCompound(EXPLORE_TAG));
        boss.fromTag(tag.getCompound(BOSS_TAG));
    }

    public void setDirty(boolean flag) {
        this.dirty = flag;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public boolean isActive() {
        return getTimeLeft() > 0;
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

    public int getExpiry() {
        return expiry;
    }

    public int getTimeLeft() {
        return Math.max(0, expiry - time);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getHint() {
        return hint;
    }

    public UUID getMerchant() {
        return merchant;
    }

    public UUID getOwner() {
        return owner;
    }

    public Reward getReward() {
        return reward;
    }

    public Gather getGather() {
        return gather;
    }

    public Hunt getHunt() {
        return hunt;
    }

    public Explore getExplore() {
        return explore;
    }

    public Boss getBoss() {
        return boss;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void tick(int currentTime) {
        this.time = currentTime;
    }

    public void playerTick(PlayerEntity player) {
        explore.playerTick(player);
        boss.playerTick(player);
    }

    public void complete(PlayerEntity player, MerchantEntity merchant) {
        gather.complete(player, merchant);
        explore.complete(player, merchant);
        reward.complete(player, merchant);
        boss.complete(player, merchant);

        this.expiry = 0; // isActive() will no longer be true
        this.setDirty(true);
    }

    public void abandon(PlayerEntity player) {
        boss.abandon(player);

        this.expiry = 0; // isActive() will no longer be true
        this.setDirty(true);
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

    public void playerKilledEntity(PlayerEntity player, LivingEntity entity) {
        hunt.playerKilledEntity(player, entity);
        boss.playerKilledEntity(player, entity);
    }

    public static Quest getFromTag(CompoundTag fromTag) {
        Quest quest = new Quest();
        quest.fromTag(fromTag);
        return quest;
    }
}
