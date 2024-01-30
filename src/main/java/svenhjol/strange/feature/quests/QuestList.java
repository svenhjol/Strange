package svenhjol.strange.feature.quests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestList {
    public static final String QUESTS_TAG = "quests";
    private List<Quest> quests = new ArrayList<>();

    public List<Quest> all() {
        return quests;
    }

    public Optional<Quest> get(String id) {
        return quests.stream()
            .filter(q -> q.id().equals(id)).findFirst();
    }

    public void add(Quest quest) {
        quests.add(quest);
    }

    public void remove(Quest quest) {
        remove(quest.id());
    }

    public void remove(String questId) {
        var opt = get(questId);
        if (opt.isEmpty()) {
            return;
        }

        quests.remove(opt.get());
    }

    public int size() {
        return quests.size();
    }

    public boolean isEmpty() {
        return quests.isEmpty();
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var list = new ListTag();

        for (var quest : quests) {
            var questTag = new CompoundTag();
            quest.save(questTag);
            list.add(questTag);
        }

        tag.put(QUESTS_TAG, list);
        return tag;
    }

    public static QuestList load(CompoundTag tag) {
        var quests = new QuestList();
        var list = tag.getList(QUESTS_TAG, 10);
        quests.quests = list.stream()
            .map(t -> Quest.load((CompoundTag)t))
            .collect(Collectors.toCollection(ArrayList::new));

        return quests;
    }
}
