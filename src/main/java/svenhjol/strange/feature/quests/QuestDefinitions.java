package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.type.GatherType;
import svenhjol.strange.feature.quests.type.HuntType;

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
            public GatherType type() { return GatherType.instance(); }

            @Override
            public List<VillagerProfession> professions() { return List.of(profession); }

            @Override
            public List<Integer> levels() { return List.of(level); }

            @Override
            public List<ResourceLocation> pools() { return pools.stream().map(QuestDefinitions::makePool).toList(); }
        };
    }

    static IQuestDefinition makeHunt(VillagerProfession profession, int level, List<String> pools) {
        return new IQuestDefinition() {
            @Override
            public HuntType type() { return HuntType.instance(); }

            @Override
            public List<VillagerProfession> professions() { return List.of(profession); }

            @Override
            public List<Integer> levels() { return List.of(level); }

            @Override
            public List<ResourceLocation> pools() { return pools.stream().map(QuestDefinitions::makePool).toList(); }
        };
    }

    static ResourceLocation makePool(String id) {
        return new ResourceLocation(Strange.ID, id);
    }
}
