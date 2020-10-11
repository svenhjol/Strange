package svenhjol.strange.scroll;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.scroll.populator.*;
import svenhjol.strange.scroll.tag.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class QuestManager extends PersistentState {
    public static final String TICK_TAG = "Tick";
    public static final String QUESTS_TAG = "Quests";

    private int currentTime;
    private final Map<String, Quest> quests = new HashMap<>();
    private final Map<UUID, List<Quest>> playerQuests = new HashMap<>();

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

        // this is regenerated when list tags are loaded
        playerQuests.clear();

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

    @Nullable
    public Quest getQuest(String id) {
        Quest quest = quests.getOrDefault(id, null);

        if (quest != null && quest.isActive())
            return quest;

        return null;
    }

    public void createQuest(ItemStack scroll, ServerPlayerEntity player, JsonDefinition definition) {
        UUID merchant = ScrollItem.getScrollMerchant(scroll);
        UUID owner = player.getUuid();

        int rarity = Math.min(1, ScrollItem.getScrollRarity(scroll));
        Quest quest = new Quest(definition, owner, merchant, rarity, currentTime);

        List<Populator> populators = new ArrayList<>(Arrays.asList(
            new LangPopulator(player, quest, definition),
            new RewardPopulator(player, quest, definition),
            new GatherPopulator(player, quest, definition),
            new HuntPopulator(player, quest, definition),
            new ExplorePopulator(player, quest, definition),
            new BossPopulator(player, quest, definition)
        ));

        populators.forEach(Populator::populate);

        // set scroll name and quest ID
        ScrollItem.setScrollName(scroll, new TranslatableText(quest.getTitle()));
        ScrollItem.setScrollQuest(scroll, quest.getId());

        // add new quest to the active quests
        addQuest(quest);
    }

    public boolean isPresent(String id) {
        return quests.containsKey(id);
    }

    public static String nameFor(DimensionType dimensionType) {
        return "quests" + dimensionType.getSuffix();
    }

    private void addQuest(Quest quest) {
        if (quest.isActive()) {
            quests.put(quest.getId(), quest);

            // generate transient player quest mapping
            UUID owner = quest.getOwner();

            if (!playerQuests.containsKey(owner))
                playerQuests.put(owner, new ArrayList<>());

            playerQuests.get(owner).add(quest);
        }

        this.markDirty();
    }
}
