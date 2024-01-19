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
        Quests.registerDefinition(makeGather(VillagerProfession.FLETCHER, 1, List.of(Pair.of("quest/novice_fletcher_gathers", 10)), List.of(Pair.of("quest/novice_rewards", 5))));
//        Quests.registerDefinition(makeHunt(VillagerProfession.FLETCHER, 1, List.of("novice_fletcher_hunt_mobs")));
//        Quests.registerDefinition(makeGather(VillagerProfession.FLETCHER, 2, List.of("apprentice_fletcher_gather_items")));
//        Quests.registerDefinition(makeHunt(VillagerProfession.FLETCHER, 2, List.of("apprentice_fletcher_hunt_mobs")));
    }

    static IQuestDefinition makeGather(VillagerProfession profession, int level, List<Pair<String, Integer>> requirements, List<Pair<String, Integer>> rewards) {
        return new IQuestDefinition() {
            @Override
            public QuestType type() { return QuestType.GATHER; }

            @Override
            public VillagerProfession profession() { return profession; }

            @Override
            public int level() { return level; }

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