package svenhjol.strange.feature.quests;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.Strange;

import java.util.List;

public class QuestDefinitions {
    public static void init() {
        fletcher();
    }

    static void fletcher() {
        Quests.registerDefinition(make(QuestType.GATHER, VillagerProfession.FLETCHER, 1, List.of(Pair.of("quest/novice_fletcher_gathers", 10)), List.of(Pair.of("quest/common_novice_rewards", 5), Pair.of("quest/uncommon_novice_rewards", 1)), false));
        Quests.registerDefinition(make(QuestType.ARTIFACT, VillagerProfession.FLETCHER, 1, List.of(Pair.of("novice_artifact_loot_tables", 1)), List.of(Pair.of("quest/common_apprentice_rewards", 6), Pair.of("quest/uncommon_apprentice_rewards", 2)), false));
        Quests.registerDefinition(make(QuestType.GATHER, VillagerProfession.FLETCHER, 1, List.of(Pair.of("quest/novice_fletcher_gathers", 30)), List.of(Pair.of("quest/common_novice_rewards", 10), Pair.of("quest/uncommon_novice_rewards", 10)), true));
        Quests.registerDefinition(make(QuestType.HUNT, VillagerProfession.TOOLSMITH, 1, List.of(Pair.of("quest/novice_toolsmith_hunts", 5)), List.of(Pair.of("quest/common_novice_rewards", 5), Pair.of("quest/uncommon_novice_rewards", 1)), false));
    }

    static QuestDefinition make(QuestType type, VillagerProfession profession, int level, List<Pair<String, Integer>> requirements, List<Pair<String, Integer>> rewards, boolean isEpic) {
        return new QuestDefinition() {
            @Override
            public QuestType type() { return type; }

            @Override
            public VillagerProfession profession() { return profession; }

            @Override
            public int level() { return level; }

            @Override
            public boolean isEpic() {
                return isEpic;
            }

            @Override
            public int requiredLoyalty() {
                return isEpic ? 2 : 0;
            }

            @Override
            public int experience() {
                var def = QuestDefinition.super.experience();
                return isEpic ? def * 2 : def;
            }

            @Override
            public List<Pair<ResourceLocation, Integer>> requirements() { return requirements.stream().map(QuestDefinitions::makePool).toList(); }

            @Override
            public List<Pair<ResourceLocation, Integer>> rewards() {
                return rewards.stream().map(QuestDefinitions::makePool).toList();
            }
        };
    }

    static Pair<ResourceLocation, Integer> makePool(Pair<String, Integer> id) {
        return Pair.of(new ResourceLocation(Strange.ID, id.getFirst()), id.getSecond());
    }
}
