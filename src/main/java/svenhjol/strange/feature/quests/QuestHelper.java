package svenhjol.strange.feature.quests;

import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestHelper {
    public static List<IQuestDefinition> getDefinitions(VillagerProfession profession, Integer level) {
        return Quests.DEFINITIONS.stream()
            .filter(d -> d.profession() == profession)
            .filter(d -> d.level() == level)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Quest<?>> makeQuests(List<IQuestDefinition> definitions, int max) {
        List<Quest<?>> quests = new ArrayList<>();

        if (!definitions.isEmpty()) {
            definitions = definitions.subList(0, Math.min(definitions.size(), max));
        }

        for (IQuestDefinition def : definitions) {
            quests.add(Quest.create(def));
        }

        return quests;
    }
}
