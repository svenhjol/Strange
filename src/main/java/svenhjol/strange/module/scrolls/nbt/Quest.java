package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.strange.module.scrolls.ScrollDefinition;
import svenhjol.strange.module.scrolls.ScrollHelper;

import java.util.UUID;

public class Quest implements IQuestSerializable {
    private static final String ID_NBT = "id";
    private static final String DEFINITION_NBT = "definition";
    private static final String TITLE_NBT = "title";
    private static final String DESCRIPTION_NBT = "description";
    private static final String HINT_NBT = "hint";
    private static final String MERCHANT_NBT = "merchant";
    private static final String OWNER_NBT = "owner";
    private static final String REWARD_NBT = "reward";
    private static final String TIER_NBT = "tier";
    private static final String RARITY_NBT = "rarity";
    private static final String TIME_NBT = "time";
    private static final String EXPIRY_NBT = "expiry";
    private static final String GATHER_NBT = "gather";
    private static final String HUNT_NBT = "hunt";
    private static final String EXPLORE_NBT = "explore";
    private static final String BOSS_NBT = "boss";

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

    private final Reward reward = new Reward(this);
    private final Gather gather = new Gather(this);
    private final Hunt hunt = new Hunt(this);
    private final Explore explore = new Explore(this);
    private final Boss boss = new Boss(this);

    private Quest() { }

    public Quest(ScrollDefinition definition, UUID owner, UUID merchant, int rarity, int currentTime) {
        this.id = RandomStringUtils.randomAlphabetic(4).toLowerCase();
        this.rarity = Math.max(1, rarity);
        this.tier = definition.getTier();
        this.definition = definition.getId();
        this.time = currentTime;
        this.expiry = currentTime + definition.getTimeLimit();
        this.merchant = merchant;
        this.owner = owner;
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt(TIER_NBT, tier);
        nbt.putInt(RARITY_NBT, rarity);
        nbt.putInt(EXPIRY_NBT, expiry);
        nbt.putInt(TIME_NBT, time);
        nbt.putString(MERCHANT_NBT, merchant.toString());
        nbt.putString(OWNER_NBT, owner.toString());
        nbt.putString(ID_NBT, id);
        nbt.putString(DEFINITION_NBT, definition);
        nbt.putString(TITLE_NBT, title);
        nbt.putString(DESCRIPTION_NBT, description);
        nbt.putString(HINT_NBT, hint);
        nbt.put(REWARD_NBT, reward.toNbt());
        nbt.put(GATHER_NBT, gather.toNbt());
        nbt.put(HUNT_NBT, hunt.toNbt());
        nbt.put(EXPLORE_NBT, explore.toNbt());
        nbt.put(BOSS_NBT, boss.toNbt());

        return nbt;
    }

    public void fromNbt(CompoundTag nbt) {
        tier = nbt.getInt(TIER_NBT);
        rarity = Math.max(1, nbt.getInt(RARITY_NBT));
        expiry = Math.max(0, nbt.getInt(EXPIRY_NBT));
        time = Math.max(0, nbt.getInt(TIME_NBT));
        merchant = UUID.fromString(nbt.getString(MERCHANT_NBT));
        owner = UUID.fromString(nbt.getString(OWNER_NBT));
        id = nbt.getString(ID_NBT);
        definition = nbt.getString(DEFINITION_NBT);
        title = nbt.getString(TITLE_NBT);
        description = nbt.getString(DESCRIPTION_NBT);
        hint = nbt.getString(HINT_NBT);
        reward.fromNbt(nbt.getCompound(REWARD_NBT));
        gather.fromNbt(nbt.getCompound(GATHER_NBT));
        hunt.fromNbt(nbt.getCompound(HUNT_NBT));
        explore.fromNbt(nbt.getCompound(EXPLORE_NBT));
        boss.fromNbt(nbt.getCompound(BOSS_NBT));
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

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void tick(int currentTime) {
        this.time = currentTime;
    }

    public void playerTick(Player player) {
        explore.playerTick(player);
        boss.playerTick(player);
    }

    public void complete(Player player, AbstractVillager merchant) {
        gather.complete(player, merchant);
        explore.complete(player, merchant);
        reward.complete(player, merchant);
        boss.complete(player, merchant);

        this.expiry = 0; // isActive() will no longer be true
        this.setDirty(true);
    }

    public void abandon(Player player) {
        boss.abandon(player);

        this.expiry = 0; // isActive() will no longer be true
        this.setDirty(true);
    }

    public boolean isSatisfied(Player player) {
        update(player);
        return gather.isSatisfied()
            && hunt.isSatisfied()
            && explore.isSatisfied()
            && boss.isSatisfied();
    }

    public void update(Player player) {
        gather.update(player);
        hunt.update(player);
        explore.update(player);
        boss.update(player);
    }

    public void entityKilled(LivingEntity entity, Entity attacker) {
        hunt.entityKilled(entity, attacker);
        boss.entityKilled(entity, attacker);
    }

    public static Quest getFromNbt(CompoundTag nbt) {
        Quest quest = new Quest();
        quest.fromNbt(nbt);
        return quest;
    }
}
