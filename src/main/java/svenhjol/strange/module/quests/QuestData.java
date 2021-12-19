package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuestData extends SavedData {
    public static final String QUESTS_TAG = "quests";

    private final Map<String, Quest> quests = new ConcurrentHashMap<>();

    public QuestData(@Nullable ServerLevel level) {
        this.setDirty();
    }

    public static QuestData load(CompoundTag tag) {
        return load(null, tag);
    }

    public static QuestData load(@Nullable ServerLevel level, CompoundTag tag) {
        QuestData data = new QuestData(level);

        ListTag listTag = tag.getList(QUESTS_TAG, 10);

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag questTag = listTag.getCompound(i);
            Quest quest = new Quest(questTag);
            data.add(quest);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();

        eachQuest(quest -> {
            CompoundTag questTag = quest.save();
            listTag.add(questTag);
        });

        tag.put(QUESTS_TAG, listTag);
        return tag;
    }

    /**
     * Serialize quests for a specific player.
     * This is generally used for sending quests to the player client.
     */
    public CompoundTag save(Player player) {
        var tag = new CompoundTag();
        var listTag = new ListTag();

        eachPlayerQuest(player, quest -> {
            CompoundTag questTag = quest.save();
            listTag.add(questTag);
        });

        tag.put(QUESTS_TAG, listTag);
        return tag;
    }

    public List<Quest> all() {
        return new ArrayList<>(quests.values());
    }

    public List<Quest> all(Player player) {
        return quests.values().stream()
            .filter(q -> q.getOwner().equals(player.getUUID()))
            .collect(Collectors.toList());
    }

    @Nullable
    public Quest get(String id) {
        return quests.getOrDefault(id, null);
    }

    public boolean has(String id) {
        return quests.containsKey(id);
    }

    public void remove(Quest quest) {
        String id = quest.getId();
        quests.remove(id);
        setDirty();
    }

    public void add(Quest quest) {
        quests.put(quest.getId(), quest);
        setDirty();
    }

    public void eachQuest(Consumer<Quest> callback) {
        quests.values().forEach(callback);
    }

    public void eachPlayerQuest(Player player, Consumer<Quest> callback) {
        all(player).forEach(callback);
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_quests" + dimensionType.getFileSuffix();
    }
}
