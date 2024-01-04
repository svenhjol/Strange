package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

public interface IQuestDefinition {
    QuestType type();

    VillagerProfession profession();

    int level();

    List<ResourceLocation> pools();

    default int requiredLoyalty() {
        return 0;
    }

    default ResourceLocation randomPool(RandomSource random) {
        return pools().get(random.nextInt(pools().size()));
    }
}
