package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

public interface IQuestDefinition {
    int XP_MULTIPLIER_PER_LEVEL = 3;

    QuestType type();

    VillagerProfession profession();

    int level();

    List<ResourceLocation> requirements();

    List<ResourceLocation> rewards();

    int maxRewardStackSize();

    default int experience() {
        return level() * XP_MULTIPLIER_PER_LEVEL;
    }

    default int requiredLoyalty() {
        return 0;
    }

    default ResourceLocation randomRequirement(RandomSource random) {
        return requirements().get(random.nextInt(requirements().size()));
    }

    default ResourceLocation randomReward(RandomSource random) {
        return rewards().get(random.nextInt(rewards().size()));
    }
}
