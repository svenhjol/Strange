package svenhjol.strange.feature.quests;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;
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

    public static List<IQuestDefinition> getDefinitionsUpToLevel(VillagerProfession profession, int level, int max, RandomSource random) {
        var definitions = Quests.DEFINITIONS.stream()
            .filter(d -> d.profession() == profession)
            .filter(d -> d.level() <= level)
            .collect(Collectors.toCollection(ArrayList::new));

        Util.shuffle(definitions, random);
        return definitions.subList(0, Math.min(definitions.size(), max));
    }

    public static List<Quest<?>> makeQuests(List<IQuestDefinition> definitions) {
        List<Quest<?>> quests = new ArrayList<>();

        for (IQuestDefinition definition : definitions) {
            quests.add(Quest.create(definition));
        }

        return quests;
    }
}
