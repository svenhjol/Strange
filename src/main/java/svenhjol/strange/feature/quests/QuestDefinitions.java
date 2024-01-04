package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.Strange;

import java.util.List;

public class QuestDefinitions {
    public static void init() {
        fletcher();
    }

    static void fletcher() {
        Quests.registerDefinition(makeGather(VillagerProfession.FLETCHER, 1, List.of("novice_fletcher_gather_items")));
//        Quests.registerDefinition(makeHunt(VillagerProfession.FLETCHER, 1, List.of("novice_fletcher_hunt_mobs")));
//        Quests.registerDefinition(makeGather(VillagerProfession.FLETCHER, 2, List.of("apprentice_fletcher_gather_items")));
//        Quests.registerDefinition(makeHunt(VillagerProfession.FLETCHER, 2, List.of("apprentice_fletcher_hunt_mobs")));
    }

    static IQuestDefinition makeGather(VillagerProfession profession, int level, List<String> pools) {
        return new IQuestDefinition() {
            @Override
            public QuestType type() { return QuestType.GATHER; }

            @Override
            public VillagerProfession profession() { return profession; }

            @Override
            public int level() { return level; }

            @Override
            public List<ResourceLocation> pools() { return pools.stream().map(QuestDefinitions::makePool).toList(); }
        };
    }

    static ResourceLocation makePool(String id) {
        return new ResourceLocation(Strange.ID, id);
    }
}
