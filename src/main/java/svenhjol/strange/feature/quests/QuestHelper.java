package svenhjol.strange.feature.quests;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.client.QuestResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestHelper {
    public static List<QuestDefinition> makeDefinitions(UUID villagerUuid, VillagerProfession profession, int minLevel, int maxLevel, int numberOfDefinitions, RandomSource random) {
        var definitions = Quests.DEFINITIONS.stream()
            .filter(d -> d.profession() == profession)
            .filter(d -> d.level() >= minLevel && d.level() <= maxLevel)
            .filter(d -> d.requiredLoyalty() <= Quests.getLoyalty(villagerUuid))
            .collect(Collectors.toCollection(ArrayList::new));

        if (definitions.isEmpty()) {
            return List.of();
        }

        // Get epics separately.
        var epics = definitions.stream().filter(QuestDefinition::isEpic).toList();

        Util.shuffle(definitions, random);
        var sublist = definitions.subList(0, Math.min(definitions.size(), numberOfDefinitions));

        while (sublist.size() < numberOfDefinitions) {
            sublist.add(definitions.get(random.nextInt(definitions.size())));
        }

        // Replace one of the definitions with the epic.
        if (!epics.isEmpty() && sublist.stream().noneMatch(QuestDefinition::isEpic)) {
            var epic = random.nextInt(epics.size());
            var index = random.nextInt(sublist.size());
            sublist.set(index, epics.get(epic));
        }

        return sublist;
    }

    public static List<Quest> makeQuestsFromDefinitions(ResourceManager manager, List<QuestDefinition> definitions, UUID villagerUuid) {
        List<Quest> quests = new ArrayList<>();

        for (QuestDefinition definition : definitions) {
            quests.add(Quest.create(manager, definition, villagerUuid));
        }

        return quests;
    }

    public static Optional<Villager> getNearbyMatchingVillager(Level level, BlockPos pos, UUID villagerUuid) {
        var nearby = level.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(8.0d));
        return nearby.stream().filter(e -> e.getUUID().equals(villagerUuid)).findFirst();
    }

    public static List<Villager> getNearbyMatchingProfessions(Level level, BlockPos pos, VillagerProfession profession) {
        var nearby = level.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(8.0d));
        return nearby.stream()
            .filter(e -> e.getVillagerData().getProfession().equals(profession))
            .collect(Collectors.toCollection(ArrayList::new));
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

    public static Component makeVillagerTitle(VillagerProfession profession) {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        var key = registry.getKey(profession);
        return TextHelper.translatable(QuestResources.QUEST_OFFERS_TITLE_KEY,
            TextHelper.translatable("entity." + key.getNamespace() + ".villager." + key.getPath()));
    }

    public static Component makeQuestTitle(Quest quest) {
        return TextHelper.translatable(QuestResources.QUEST_TITLE_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            quest.type().getTypeName());
    }
}
