package svenhjol.strange.module.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuestData extends SavedData {
    public static final String TAG_QUESTS = "quests";

    private final Map<String, Quest> quests = new ConcurrentHashMap<>();

    public QuestData(@Nullable ServerLevel level) {
        setDirty();
    }

    public static QuestData fromNbt(CompoundTag tag) {
        return fromNbt(null, tag);
    }

    public static QuestData fromNbt(@Nullable ServerLevel level, CompoundTag tag) {
        QuestData data = new QuestData(level);

        ListTag listTag = tag.getList(TAG_QUESTS, 10);
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
            CompoundTag questTag = quest.toNbt();
            listTag.add(questTag);
        });

        tag.put(TAG_QUESTS, listTag);
        return tag;
    }

    /**
     * Serialize quests for a specific player.
     * This is generally used for sending quests to the player client.
     */
    public CompoundTag saveForPlayer(Player player, CompoundTag tag) {
        ListTag listTag = new ListTag();

        eachPlayerQuest(player, quest -> {
            CompoundTag questTag = quest.toNbt();
            listTag.add(questTag);
        });

        tag.put(TAG_QUESTS, listTag);
        return tag;
    }

    public List<Quest> getAll() {
        return new ArrayList<>(quests.values());
    }

    public List<Quest> getAll(Player player) {
        return this.quests.values().stream()
            .filter(q -> q.getOwner().equals(player.getUUID()))
            .collect(Collectors.toList());
    }

    public Optional<Quest> get(String id) {
        Quest quest = quests.getOrDefault(id, null);
        return Optional.ofNullable(quest);
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
        getAll(player).forEach(callback);
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_quests" + dimensionType.getFileSuffix();
    }

    @Override
    public void save(File file) {
        super.save(file);
    }
}
