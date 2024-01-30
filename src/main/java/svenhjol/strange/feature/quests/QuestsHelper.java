package svenhjol.strange.feature.quests;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.*;
import java.util.stream.Collectors;

public class QuestsHelper {
    public static final Map<Integer, String> TIERS = new HashMap<>();
    public static List<QuestDefinition> makeDefinitions(UUID villagerUuid, VillagerProfession profession, int minLevel, int maxLevel, int numberOfDefinitions, RandomSource random) {
        var definitions = Quests.DEFINITIONS.stream()
            .filter(d -> d.profession() == profession || d.profession() == VillagerProfession.NONE)
            .filter(d -> d.level() >= minLevel && d.level() <= maxLevel)
            .filter(d -> d.loyalty() <= Quests.getLoyalty(villagerUuid))
            .collect(Collectors.toCollection(ArrayList::new));

        if (definitions.isEmpty()) {
            return List.of();
        }

        // Get epics separately.
        var epics = definitions.stream().filter(QuestDefinition::epic).toList();

        Util.shuffle(definitions, random);
        var sublist = definitions.subList(0, Math.min(definitions.size(), numberOfDefinitions));

        while (sublist.size() < numberOfDefinitions) {
            sublist.add(definitions.get(random.nextInt(definitions.size())));
        }

        // Replace one of the definitions with the epic.
        if (!epics.isEmpty() && sublist.stream().noneMatch(QuestDefinition::epic)) {
            var epic = random.nextInt(epics.size());
            var index = random.nextInt(sublist.size());
            sublist.set(index, epics.get(epic));
        }

        return sublist;
    }

    public static QuestList makeQuestsFromDefinitions(ResourceManager manager, List<QuestDefinition> definitions, UUID villagerUuid) {
        var quests = new QuestList();

        for (var definition : definitions) {
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
            .filter(e -> e.getVillagerData().getProfession().equals(profession) || e.getVillagerData().getProfession().equals(VillagerProfession.NONE))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static String getVillagerLevelName(int villagerLevel) {
        return TIERS.getOrDefault(villagerLevel, "any");
    }

    public static ResourceLocation getVillagerProfessionId(VillagerProfession villagerProfession) {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        return registry.getKey(villagerProfession);
    }

    public static String getVillagerProfessionName(VillagerProfession villagerProfession) {
        if (villagerProfession.equals(VillagerProfession.NONE)) {
            return "villager";
        }
        return getVillagerProfessionId(villagerProfession).getPath();
    }

    public static void throwItemsAtPlayer(Villager villager, Player player, List<ItemStack> items) {
        for (ItemStack stack : items) {
            BehaviorUtils.throwItem(villager, stack, player.position());
        }
    }

    public static boolean hasMaxQuests(Player player) {
        var quests = Quests.getPlayerQuests(player);
        return quests.size() >= Quests.maxPlayerQuests;
    }

    public static Component makeVillagerOffersTitle(VillagerProfession profession) {
        var registry = BuiltInRegistries.VILLAGER_PROFESSION;
        var professionKey = registry.getKey(profession);
        return TextHelper.translatable(QuestsResources.QUEST_OFFERS_TITLE_KEY,
            TextHelper.translatable("entity." + professionKey.getNamespace() + ".villager." + professionKey.getPath()));
    }

    public static Component makeQuestTitle(Quest quest) {
        return TextHelper.translatable(quest.isEpic() ? QuestsResources.EPIC_QUEST_TITLE_KEY : QuestsResources.QUEST_TITLE_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            quest.type().getTypeName());
    }

    public static Component makeQuestTitleWithProfession(Quest quest) {
        var professionId = getVillagerProfessionId(quest.villagerProfession());
        return TextHelper.translatable(quest.isEpic() ? QuestsResources.EPIC_QUEST_TITLE_WITH_PROFESSION_KEY : QuestsResources.QUEST_TITLE_WITH_PROFESSION_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            TextHelper.translatable("entity." + professionId.getNamespace() + ".villager." + professionId.getPath()),
            quest.type().getTypeName());
    }

    static {
        TIERS.put(1, "novice");
        TIERS.put(2, "apprentice");
        TIERS.put(3, "journeyman");
        TIERS.put(4, "expert");
        TIERS.put(5, "master");
    }
}
