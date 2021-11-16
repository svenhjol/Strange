package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.strange.module.quests.component.IQuestComponent;
import svenhjol.strange.module.quests.component.Reward;

import javax.annotation.Nullable;
import java.util.*;

public class Quest  implements IQuestComponent {
    public static final String TAG_ID = "id";
    public static final String TAG_DEFINITION = "definition";
    public static final String TAG_MERCHANT = "merchant";
    public static final String TAG_PLAYER = "player";
    public static final String TAG_STATE = "state";
    public static final String TAG_DIFFICULTY = "difficulty";

    public static final int ID_LENGTH = 4;

    private State state = State.CREATED;
    private String id;
    private QuestDefinition definition;
    private UUID owner;
    private UUID merchant;
    private Random random;
    private float difficulty;
    private boolean dirty;

    private final List<IQuestComponent> components = new LinkedList<>();

    private Quest() {
        components.add(new Reward(this));
    }

    public Quest(CompoundTag tag) {
        this();
        this.fromNbt(tag);
    }

    public Quest(QuestDefinition definition, float difficulty, @Nullable AbstractVillager merchant) {
        this();
        this.id = RandomStringUtils.randomAlphabetic(ID_LENGTH).toLowerCase(Locale.ROOT);
        this.definition = definition;
        this.difficulty = difficulty;
        this.merchant = merchant != null ? merchant.getUUID() : QuestHelper.ANY_UUID;
        this.owner = QuestHelper.ANY_UUID;
        this.random = new Random(this.id.hashCode());

        getQuests().add(this);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return definition.getTitle();
    }

    public String getDescription() {
        return definition.getDescription();
    }

    public String getHint() {
        return definition.getHint();
    }

    public Random getRandom() {
        return random;
    }

    public int getTier() {
        return definition.getTier();
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getMerchant() {
        return merchant;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public Reward getReward() {
        return (Reward)components.stream().filter(c -> c instanceof Reward).findFirst().orElseThrow();
    }

    public void playerTick(ServerPlayer player) {
        components.forEach(c -> c.playerTick(player));
    }

    public boolean start(ServerPlayer player) {
        if (state == State.CREATED) {

            // do first-time population of each component
            components.forEach(c -> {
                if (!c.start(player)) {
                    throw new IllegalStateException("Could not initialise quest component: " + c.getId());
                }
            });

        } else if (state != State.PAUSED) {
            throw new IllegalStateException("Quest should be in PAUSED or CREATED state, was in " + state.getSerializedName());
        }

        this.owner = player.getUUID();
        state = State.STARTED;
        Quests.sendToast(player, QuestToast.QuestToastType.STARTED, getTier(), new TextComponent(getTitle()));
        setDirty();

        return true;
    }

    public void abandon(ServerPlayer player) {
        components.forEach(c -> c.abandon(player));
        Quests.sendToast(player, QuestToast.QuestToastType.ABANDONED, getTier(), new TextComponent(getTitle()));
        getQuests().remove(this);
    }

    public void complete(ServerPlayer player, @Nullable AbstractVillager merchant) {
        components.forEach(c -> c.complete(player, merchant));
        Quests.sendToast(player, QuestToast.QuestToastType.COMPLETED, getTier(), new TextComponent(getTitle()));
        getQuests().remove(this);
    }

    public void pause() {
        owner = QuestHelper.ANY_UUID;
        state = State.PAUSED;
        setDirty();
    }

    public void update(ServerPlayer player) {
        components.forEach(c -> c.update(player));
    }

    public boolean isSatisfied(ServerPlayer player) {
        update(player);
        return components.stream().allMatch(q -> q.isSatisfied(player));
    }

    public QuestDefinition getDefinition() {
        if (definition == null) {
            throw new IllegalStateException("Definition is null for quest " + getId());
        }
        return definition;
    }

    public void setDirty() {
        dirty = true;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        if (definition == null) {
            return tag;
        }

        tag.putString(TAG_ID, id);
        tag.putString(TAG_DEFINITION, definition.getId());
        tag.putString(TAG_MERCHANT, merchant.toString());
        tag.putString(TAG_PLAYER, owner.toString());
        tag.putString(TAG_STATE, state.getSerializedName());
        tag.putFloat(TAG_DIFFICULTY, difficulty);

        components.forEach(c -> tag.put(c.getId(), c.toNbt()));
        return tag;
    }

    @Override
    public void fromNbt(CompoundTag tag) {
        id = tag.getString(TAG_ID);
        definition = Quests.getDefinition(tag.getString(TAG_DEFINITION));
        merchant = UUID.fromString(tag.getString(TAG_MERCHANT));
        owner = UUID.fromString(tag.getString(TAG_PLAYER));
        difficulty = tag.getFloat(TAG_DIFFICULTY);
        random = new Random(id.hashCode());
        components.forEach(c -> c.fromNbt(tag.getCompound(c.getId())));
    }

    private QuestData getQuests() {
        return Quests.getQuestData().orElseThrow();
    }

    private enum State implements ICharmEnum {
        CREATED,
        STARTED,
        PAUSED;
    }
}
