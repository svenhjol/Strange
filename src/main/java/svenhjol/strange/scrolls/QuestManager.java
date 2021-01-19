package svenhjol.strange.scrolls;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.charm.Charm;
import svenhjol.strange.scrolls.populator.*;
import svenhjol.strange.scrolls.tag.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuestManager extends PersistentState {
    public static final String TICK_TAG = "Tick";
    public static final String QUESTS_TAG = "Quests";
    public static final int DEFAULT_EXPIRY = 300000; // in minutes. roughly a month
    public static final int MAX_PLAYER_QUESTS = 5; // maybe this could be configurable?

    private int currentTime;
    private final Map<String, Quest> quests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> playerQuests = new ConcurrentHashMap<>();

    public QuestManager(ServerWorld world) {
        super(nameFor(world.getDimension()));
        markDirty();
    }

    public void tick() {
        quests.values().forEach(quest -> {
            if (!quest.isActive())
                return;

            quest.tick(currentTime);

            if (quest.isDirty()) {
                quest.setDirty(false);
                this.markDirty();
            }
        });

        // required at interval so that the current time gets written into tags properly
        if (++currentTime % 200 == 0)
            markDirty();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        currentTime = tag.getInt(TICK_TAG);
        ListTag listTag = tag.getList(QUESTS_TAG, 10);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag questTag = listTag.getCompound(i);
            Quest quest = Quest.getFromTag(questTag);
            addQuest(quest);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
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
    public void markDirty() {
        super.markDirty();
        regeneratePlayerQuests();
    }

    public void forEachQuest(Consumer<Quest> callback) {
        quests.values().forEach(quest -> {
            if (quest.isActive())
                callback.accept(quest);
        });
    }

    public void forEachPlayerQuest(ServerPlayerEntity player, Consumer<Quest> callback) {
        UUID owner = player.getUuid();

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

    public List<Quest> getQuests(PlayerEntity player) {
        UUID owner = player.getUuid();
        return playerQuests.containsKey(owner)
            ? playerQuests.get(owner).stream().filter(Quest::isActive).collect(Collectors.toList())
            : new ArrayList<>();
    }

    public List<Quest> getQuests() {
        return quests.values().stream().filter(Quest::isActive).collect(Collectors.toList());
    }

    public void sendToast(PlayerEntity player, Quest quest, QuestToastType type, String title) {
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeCompoundTag(quest.toTag());
        data.writeEnumConstant(type);
        data.writeString(title);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Scrolls.MSG_CLIENT_SHOW_QUEST_TOAST, data);
    }

    public boolean abandonQuest(PlayerEntity player, String id) {
        Quest quest = quests.getOrDefault(id, null);

        if (quest != null) {
            quest.abandon(player);
            this.markDirty();
            return true;
        }

        return false;
    }

    public void abandonQuests(PlayerEntity player) {
        List<Quest> quests = getQuests(player);
        quests.forEach(quest -> quest.abandon(player));
        this.markDirty();
    }

    public void abandonAllQuests() {
        quests.clear();
        this.markDirty();
    }

    public boolean checkPlayerCanStartQuest(ServerPlayerEntity player) {
        if (playerQuests.getOrDefault(player.getUuid(), new ArrayList<>()).size() >= MAX_PLAYER_QUESTS) {
            player.sendMessage(new TranslatableText("scroll.strange.too_many_quests"), true);
            return false;
        }

        return true;
    }

    @Nullable
    public Quest createQuest(ServerPlayerEntity player, ScrollDefinition definition, int rarity, @Nullable UUID seller) {
        UUID owner = player.getUuid();

        if (seller == null)
            seller = ScrollsHelper.ANY_UUID;

        Quest quest = new Quest(definition, owner, seller, rarity, currentTime);
        List<Populator> populators = getPopulatorsForQuest(player, quest);

        try {
            populators.forEach(Populator::populate);
        } catch (Exception e) {
            Charm.LOG.warn(e.getMessage());
            return null;
        }

        // add new quest to the active quests
        addQuest(quest);

        this.markDirty();
        return quest;
    }

    public List<Populator> getPopulatorsForQuest(ServerPlayerEntity player, Quest quest) {
        List<Populator> populators = new ArrayList<>(Arrays.asList(
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
        return "quests" + dimensionType.getSuffix();
    }

    private void addQuest(Quest quest) {
        if (quest.isActive())
            quests.put(quest.getId(), quest);

        this.markDirty();
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
