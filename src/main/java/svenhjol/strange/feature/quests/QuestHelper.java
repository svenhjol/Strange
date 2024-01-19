package svenhjol.strange.feature.quests;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.helper.TextHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public static List<Quest<?>> makeQuestsFromDefinitions(List<IQuestDefinition> definitions, UUID villagerUuid) {
        List<Quest<?>> quests = new ArrayList<>();

        for (IQuestDefinition definition : definitions) {
            quests.add(Quest.create(definition, villagerUuid));
        }

        return quests;
    }

    public static Optional<Villager> getNearbyVillager(Level level, BlockPos pos, UUID villagerUuid) {
        var nearby = level.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(4.0d));
        return nearby.stream().filter(e -> e.getUUID().equals(villagerUuid)).findFirst();
    }

    public static void throwItemsAtPlayer(Villager villager, Player player, List<ItemStack> items) {
        for (ItemStack stack : items) {
            BehaviorUtils.throwItem(villager, stack, player.position());
        }
    }

    public static boolean hasMaxQuests(Player player) {
        var quests = Quests.PLAYER_QUESTS.getOrDefault(player.getUUID(), List.of());
        return quests.size() >= Quests.maxPlayerQuests;
    }

    public static Component makeTitle(Quest<?> quest) {
        return TextHelper.translatable(QuestResources.QUEST_TITLE_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            quest.type().getTypeName());
    }
}
