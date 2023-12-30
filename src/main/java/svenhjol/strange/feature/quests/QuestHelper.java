package svenhjol.strange.feature.quests;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

import java.util.ArrayList;
import java.util.List;

public class QuestHelper {
    public static List<Quest> getVillagerQuests(ServerPlayer player, Villager villager) {
        var profession = villager.getVillagerData().getProfession();
        var level = villager.getVillagerData().getLevel();
        List<Quest> quests = new ArrayList<>();

        // Get definitions.
        List<IQuestDefinition> matchedDefinitions = new ArrayList<>();

        Quests.DEFINITIONS.forEach(def -> {
            if (def.levels().contains(level) && def.professions().contains(profession)) {
                matchedDefinitions.add(def);
            }
        });

        for (IQuestDefinition definition : matchedDefinitions) {
            quests.add(new Quest(definition));
        }

        return quests;
    }
}
