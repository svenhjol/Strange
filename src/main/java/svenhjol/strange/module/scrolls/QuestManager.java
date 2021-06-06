package svenhjol.strange.module.scrolls;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.charm.Charm;
import svenhjol.strange.module.scrolls.populator.*;
import svenhjol.strange.module.scrolls.tag.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuestManager extends SavedData {
    public static final String TICK_TAG = "Tick";
    public static final String QUESTS_TAG = "Quests";
    public static final int DEFAULT_EXPIRY = 300000; // in minutes. roughly a month
    public static final int MAX_PLAYER_QUESTS = 5; // maybe this could be configurable?

    private int currentTime;
    private final Level world;
    private final Map<String, Quest> quests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> playerQuests = new ConcurrentHashMap<>();

    public QuestManager(ServerLevel world) {
        this.world = world;
        setDirty();
    }

    public void tick() {
        quests.values().forEach(quest -> {
            if (!quest.isActive())
                return;

            quest.tick(currentTime);

            if (quest.isDirty()) {
                quest.setDirty(false);
                this.setDirty();
            }
        });

        // required at interval so that the current time gets written into tags properly
        if (++currentTime % 200 == 0)
            setDirty();
    }

    public static QuestManager fromNbt(ServerLevel world, CompoundTag tag) {
        QuestManager questManager = new QuestManager(world);
        questManager.currentTime = tag.getInt(TICK_TAG);
        ListTag listTag = tag.getList(QUESTS_TAG, 10);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag questTag = listTag.getCompound(i);
            Quest quest = Quest.getFromTag(questTag);
            questManager.addQuest(quest);
        }

        return questManager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();

        forEachQuest(quest -> {
            CompoundTag questTag = quest.toTag();
            listTag.add(questTag);
        });


        tag.putInt(TICK_TAG, currentTime);
        tag.put(QUESTS_TAG, listTag);
        return tag;
    }

    @Override
    public void setDirty() {
        super.setDirty();
        regeneratePlayerQuests();
    }

    public void forEachQuest(Consumer<Quest> callback) {
        quests.values().forEach(quest -> {
            if (quest.isActive())
                callback.accept(quest);
        });
    }

    public void forEachPlayerQuest(ServerPlayer player, Consumer<Quest> callback) {
        UUID owner = player.getUUID();

        if (!playerQuests.containsKey(owner))
            return;

        playerQuests.get(owner).forEach(quest -> {
            if (quest.isActive())
                callback.accept(quest);
        });
    }

    public Optional<Quest> getQuest(String id) {
        Quest quest = quests.getOrDefault(id, null);
        return quest == null || !quest.isActive() ? Optional.empty() : Optional.of(quest);
    }

    public Optional<Quest> getQuest(Quest questIn) {
        Quest quest = quests.getOrDefault(questIn.getId(), null);
        return quest != null ? Optional.of(quest) : Optional.empty();
    }

    public List<Quest> getQuests(Player player) {
        UUID owner = player.getUUID();
        return playerQuests.containsKey(owner)
            ? playerQuests.get(owner).stream().filter(Quest::isActive).collect(Collectors.toList())
            : new ArrayList<>();
    }

    public List<Quest> getQuests() {
        return quests.values().stream().filter(Quest::isActive).collect(Collectors.toList());
    }

    public void sendToast(ServerPlayer player, Quest quest, QuestToastType type, String title) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeNbt(quest.toTag());
        data.writeEnum(type);
        data.writeUtf(title);
        ServerPlayNetworking.send(player, Scrolls.MSG_CLIENT_SHOW_QUEST_TOAST, data);
    }

    public boolean abandonQuest(Player player, String id) {
        Quest quest = quests.getOrDefault(id, null);

        if (quest != null) {
            quest.abandon(player);
            this.setDirty();
            return true;
        }

        return false;
    }

    public void abandonQuests(Player player) {
        List<Quest> quests = getQuests(player);
        quests.forEach(quest -> quest.abandon(player));
        this.setDirty();
    }

    public void abandonAllQuests() {
        quests.clear();
        this.setDirty();
    }

    public boolean checkPlayerCanStartQuest(ServerPlayer player) {
        if (playerQuests.getOrDefault(player.getUUID(), new ArrayList<>()).size() >= MAX_PLAYER_QUESTS) {
            player.displayClientMessage(new TranslatableComponent("scroll.strange.too_many_quests"), true);
            return false;
        }

        return true;
    }

    @Nullable
    public Quest createQuest(ServerPlayer player, ScrollDefinition definition, int rarity, @Nullable UUID seller) {
        UUID owner = player.getUUID();

        if (seller == null)
            seller = ScrollsHelper.ANY_UUID;

        Quest quest = new Quest(definition, owner, seller, rarity, currentTime);
        List<BasePopulator> populators = getPopulatorsForQuest(player, quest);

        try {
            populators.forEach(BasePopulator::populate);
        } catch (Exception e) {
            Charm.LOG.warn(e.getMessage());
            return null;
        }

        // add new quest to the active quests
        addQuest(quest);

        this.setDirty();
        return quest;
    }

    public List<BasePopulator> getPopulatorsForQuest(ServerPlayer player, Quest quest) {
        List<BasePopulator> populators = new ArrayList<>(Arrays.asList(
            new LangPopulator(player, quest),
            new RewardPopulator(player, quest),
            new GatherPopulator(player, quest),
            new HuntPopulator(player, quest),
            new ExplorePopulator(player, quest),
            new BossPopulator(player, quest)
        ));

        return populators;
    }

    public static String nameFor(DimensionType dimensionType) {
        return "quests" + dimensionType.getFileSuffix();
    }

    private void addQuest(Quest quest) {
        if (quest.isActive())
            quests.put(quest.getId(), quest);

        this.setDirty();
    }

    private void regeneratePlayerQuests() {
        playerQuests.clear();

        quests.forEach((id, quest) -> {
            if (!quest.isActive())
                return;

            UUID owner = quest.getOwner();

            if (!playerQuests.containsKey(owner))
                playerQuests.put(owner, new ArrayList<>());

            playerQuests.get(owner).add(quest);
        });
    }
}
