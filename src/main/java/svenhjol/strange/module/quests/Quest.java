package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.RandomStringUtils;
import svenhjol.charm.enums.ICharmEnum;
import svenhjol.strange.iface.ISerializable;
import svenhjol.strange.module.quests.component.GatherComponent;
import svenhjol.strange.module.quests.component.HuntComponent;
import svenhjol.strange.module.quests.component.RewardComponent;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.event.QuestEvents;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.runes.Tier;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unchecked")
public class Quest implements ISerializable {
    public static final String ID_TAG = "id";
    public static final String DEFINITION_TAG = "definition";
    public static final String PLAYER_TAG = "player";
    public static final String STATE_TAG = "state";
    public static final String DIFFICULTY_TAG = "difficulty";
    public static final int ID_LENGTH = 6;

    private State state = State.CREATED;
    private String id;
    private QuestDefinition definition;
    private UUID owner;
    private Random random;
    private float difficulty;
    private List<IQuestComponent> components = new LinkedList<>();

    private Quest() {
        components.add(new GatherComponent(this));
        components.add(new HuntComponent(this));
        components.add(new RewardComponent(this));
    }

    public Quest(CompoundTag tag) {
        this();
        this.load(tag);
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

    public Quest copy() {
        var copy = new Quest();

        copy.state = state;
        copy.id = id;
        copy.definition = definition;
        copy.owner = owner;
        copy.random = random;
        copy.difficulty = difficulty;
        copy.components = components;

        return copy;
    }

    public <T extends IQuestComponent> T getComponent(Class<? extends T> clazz) {
        return (T)components.stream().filter(c -> c.getClass() == clazz).findFirst().orElseThrow();
    }

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

    public void abandon(Player player) {
        components.forEach(c -> c.abandon(player));
        var copy = copy();

        remove(player);

        if (!player.level.isClientSide) {
            QuestEvents.ABANDON.invoker().invoke(copy, (ServerPlayer)player);
        }
    }

    public void complete(Player player, @Nullable AbstractVillager merchant) {
        components.forEach(c -> c.complete(player, merchant));
        var copy = copy();

        remove(player);

        if (!player.level.isClientSide) {
            QuestEvents.COMPLETE.invoker().invoke(copy, (ServerPlayer)player);
        }
    }

    public void playerTick(Player player) {
        components.forEach(c -> c.playerTick(player));
        if (player.level != null && player.level.getGameTime() % 40 == 0) {
            components.forEach(c -> c.update(player));
        }
    }

    public void entityKilled(LivingEntity entity, Entity attacker) {
        components.forEach(c -> c.entityKilled(entity, attacker));
    }

    /**
     * Gives all quest component an opportunity to update their state.
     * This could be for example building dynamic maps to check if a quest is satisfied.
     * Dirty is not set here and should be set on a component level if state changes.
     */
    public void update(Player player) {
        components.forEach(c -> c.update(player));
    }

    public boolean isSatisfied(Player player) {
        update(player);
        return components.stream().allMatch(q -> q.isSatisfied(player));
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        if (definition == null) {
            return tag;
        }

        tag.putString(ID_TAG, id);
        tag.putString(DEFINITION_TAG, getDefinitionId());
        tag.putString(PLAYER_TAG, owner.toString());
        tag.putString(STATE_TAG, state.getSerializedName());
        tag.putFloat(DIFFICULTY_TAG, difficulty);

        components.forEach(c -> {
            CompoundTag componentTag = c.save();
            if (componentTag != null) {
                tag.put(c.getId(), componentTag);
            }
        });

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        id = tag.getString(ID_TAG);
        definition = Quests.getDefinition(tag.getString(DEFINITION_TAG));
        owner = UUID.fromString(tag.getString(PLAYER_TAG));
        state = State.valueOf(tag.getString(STATE_TAG).toUpperCase(Locale.ROOT));
        difficulty = tag.getFloat(DIFFICULTY_TAG);
        random = new Random(id.hashCode());
        components.forEach(c -> c.load(tag.getCompound(c.getId())));
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

    public Tier getTier() {
        return definition.getTier();
    }

    public UUID getOwner() {
        return owner;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDirty() {
        getQuests().setDirty();
    }

    private QuestData getQuests() {
        return Quests.getQuestData().orElseThrow();
    }

    private void remove(Player player) {
        getQuests().remove(this);

        if (!player.level.isClientSide) {
            QuestEvents.REMOVE.invoker().invoke(this, (ServerPlayer)player);
        }
    }

    private enum State implements ICharmEnum {
        CREATED,
        STARTED,
        PAUSED
    }
}
