package svenhjol.strange.feature.quests;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.List;

public interface QuestDefinition {
    double XP_MULTIPLIER_PER_LEVEL = 1.6d;

    QuestType type();

    VillagerProfession profession();

    int level();

    List<Pair<ResourceLocation, Integer>> requirements();

    List<Pair<ResourceLocation, Integer>> rewards();

    default int experience() {
        return (int)(level() * XP_MULTIPLIER_PER_LEVEL);
    }

    default int requiredLoyalty() {
        return 0;
    }

    default boolean isEpic() {
        return false;
    }

    default Pair<ResourceLocation, Integer> randomRequirement(RandomSource random) {
        return requirements().get(random.nextInt(requirements().size()));
    }

    default Pair<ResourceLocation, Integer> randomReward(RandomSource random) {
        return rewards().get(random.nextInt(rewards().size()));
    }
}
