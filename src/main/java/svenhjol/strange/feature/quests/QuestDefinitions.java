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
        Quests.registerDefinition(make(QuestType.GATHER, VillagerProfession.FLETCHER, 1, List.of(Pair.of("novice_fletcher_gather", 10)), List.of(Pair.of("novice_common_reward", 5), Pair.of("novice_uncommon_reward", 1)), false));
        Quests.registerDefinition(make(QuestType.GATHER, VillagerProfession.FLETCHER, 1, List.of(Pair.of("novice_fletcher_gather", 30)), List.of(Pair.of("novice_common_reward", 10), Pair.of("novice_uncommon_reward", 10)), true));
        Quests.registerDefinition(make(QuestType.ARTIFACT, VillagerProfession.FLETCHER, 1, List.of(Pair.of("novice_fletcher_artifact_loot_tables", 1)), List.of(Pair.of("apprentice_common_reward", 6), Pair.of("apprentice_uncommon_reward", 2)), false));
        Quests.registerDefinition(make(QuestType.HUNT, VillagerProfession.NONE, 1, List.of(Pair.of("novice_villager_hunt", 5)), List.of(Pair.of("novice_common_reward", 5), Pair.of("novice_uncommon_reward", 1)), false));
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
