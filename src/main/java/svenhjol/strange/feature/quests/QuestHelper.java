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

    public static List<IQuestDefinition> makeDefinitionsForVillager(VillagerProfession profession, int minLevel, int maxLevel, int numberOfDefinitions, RandomSource random) {
        var definitions = Quests.DEFINITIONS.stream()
            .filter(d -> d.profession() == profession)
            .filter(d -> d.level() >= minLevel && d.level() <= maxLevel)
            .collect(Collectors.toCollection(ArrayList::new));

        if (definitions.isEmpty()) {
            return List.of();
        }

        Util.shuffle(definitions, random);
        var sublist = definitions.subList(0, Math.min(definitions.size(), numberOfDefinitions));

        while (sublist.size() < numberOfDefinitions) {
            sublist.add(definitions.get(random.nextInt(definitions.size())));
        }
        return sublist;
    }

    public static List<Quest<?>> makeQuestsFromDefinitions(List<IQuestDefinition> definitions) {
        List<Quest<?>> quests = new ArrayList<>();

        for (IQuestDefinition definition : definitions) {
            quests.add(Quest.create(definition));
        }

        return quests;
    }
}
