package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.strange.event.QuestEvents;
import svenhjol.strange.module.quests.component.GatherComponent;
import svenhjol.strange.module.quests.component.HuntComponent;
import svenhjol.strange.module.quests.component.RewardComponent;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unchecked")
public class Quest implements IQuestComponent {
    public static final String TAG_ID = "id";
    public static final String TAG_DEFINITION = "definition";
    public static final String TAG_PLAYER = "player";
    public static final String TAG_STATE = "state";
    public static final String TAG_DIFFICULTY = "difficulty";

    public static final int ID_LENGTH = 6;

    private State state = State.CREATED;
    private String id;
    private QuestDefinition definition;
    private UUID owner;
    private Random random;
    private float difficulty;
    private boolean dirty;

    private final List<IQuestComponent> components = new LinkedList<>();

    private Quest() {
        components.add(new GatherComponent(this));
        components.add(new HuntComponent(this));
        components.add(new RewardComponent(this));
    }

    public Quest(CompoundTag tag) {
        this();
        this.fromNbt(tag);
    }

    public Quest(QuestDefinition definition, float difficulty) {
        this();
        this.id = RandomStringUtils.randomAlphabetic(ID_LENGTH).toLowerCase(Locale.ROOT);
        this.definition = definition;
        this.difficulty = difficulty;
        this.owner = QuestHelper.ANY_UUID;
        this.random = new Random(this.id.hashCode());

        getQuests().add(this);
    }

    public <T extends IQuestComponent> T getComponent(Class<? extends T> clazz) {
        return (T)components.stream().filter(c -> c.getClass() == clazz).findFirst().orElseThrow();
    }

    @Override
    public boolean start(Player player) {
        if (state == State.CREATED) {

            // do first-time population of each component
            components.forEach(c -> {
                if (!c.start(player)) {
                    throw QuestHelper.makeException(this, "Could not initialise quest component: " + c.getId());
                }
            });

        } else if (state != State.PAUSED) {
            throw QuestHelper.makeException(this, "Quest should be in PAUSED or CREATED state, was in " + state.getSerializedName());
        }

        this.owner = player.getUUID();
        state = State.STARTED;

        if (!player.level.isClientSide) {
            Quests.sendToast((ServerPlayer)player, QuestToast.QuestToastType.STARTED, getDefinitionId(), getTier());
            QuestEvents.START.invoker().invoke(this, (ServerPlayer)player);
        }

        setDirty();
        return true;
    }

    public void pause(Player player) {
        owner = QuestHelper.ANY_UUID;
        state = State.PAUSED;

        if (!player.level.isClientSide) {
            QuestEvents.PAUSE.invoker().invoke(this, (ServerPlayer) player);
        }
        setDirty();
    }

    @Override
    public void abandon(Player player) {
        components.forEach(c -> c.abandon(player));

        if (!player.level.isClientSide) {
            Quests.sendToast((ServerPlayer)player, QuestToast.QuestToastType.ABANDONED, getDefinitionId(), getTier());
            QuestEvents.ABANDON.invoker().invoke(this, (ServerPlayer)player);
        }

        getQuests().remove(this);
    }

    @Override
    public void complete(Player player, @Nullable AbstractVillager merchant) {
        components.forEach(c -> c.complete(player, merchant));

        if (!player.level.isClientSide) {
            Quests.sendToast((ServerPlayer)player, QuestToast.QuestToastType.COMPLETED, getDefinitionId(), getTier());
            QuestEvents.COMPLETE.invoker().invoke(this, (ServerPlayer)player);
        }

        getQuests().remove(this);
    }

    @Override
    public void playerTick(Player player) {
        components.forEach(c -> c.playerTick(player));
        if (player.level != null && player.level.getGameTime() % 40 == 0) {
            components.forEach(c -> c.update(player));
        }
    }

    @Override
    public void entityKilled(LivingEntity entity, Entity attacker) {
        components.forEach(c -> c.entityKilled(entity, attacker));
    }

    /**
     * Gives all quest component an opportunity to update their state.
     * This could be for example building dynamic maps to check if a quest is satisfied.
     * Dirty is not set here and should be set on a component level if state changes.
     * @param player
     */
    @Override
    public void update(Player player) {
        components.forEach(c -> c.update(player));
    }

    @Override
    public boolean isSatisfied(Player player) {
        update(player);
        return components.stream().allMatch(q -> q.isSatisfied(player));
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        if (definition == null) {
            return tag;
        }

        tag.putString(TAG_ID, id);
        tag.putString(TAG_DEFINITION, getDefinitionId());
        tag.putString(TAG_PLAYER, owner.toString());
        tag.putString(TAG_STATE, state.getSerializedName());
        tag.putFloat(TAG_DIFFICULTY, difficulty);

        components.forEach(c -> {
            CompoundTag componentTag = c.toNbt();
            if (componentTag != null) {
                tag.put(c.getId(), componentTag);
            }
        });
        return tag;
    }

    @Override
    public void fromNbt(CompoundTag tag) {
        id = tag.getString(TAG_ID);
        definition = Quests.getDefinition(tag.getString(TAG_DEFINITION));
        owner = UUID.fromString(tag.getString(TAG_PLAYER));
        state = State.valueOf(tag.getString(TAG_STATE).toUpperCase(Locale.ROOT));
        difficulty = tag.getFloat(TAG_DIFFICULTY);
        random = new Random(id.hashCode());
        components.forEach(c -> c.fromNbt(tag.getCompound(c.getId())));
    }

    public QuestDefinition getDefinition() {
        if (definition == null) {
            throw QuestHelper.makeException(this, "Definition is null for quest " + getId());
        }
        return definition;
    }

    public String getId() {
        return id;
    }

    public String getDefinitionId() {
        return definition.getId();
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

    public float getDifficulty() {
        return difficulty;
    }

    public void setDirty() {
        dirty = true;
        getQuests().setDirty();
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
