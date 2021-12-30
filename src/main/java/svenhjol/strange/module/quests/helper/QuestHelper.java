package svenhjol.strange.module.quests.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.MapHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestData;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.QuestsClient;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.exception.QuestException;
import svenhjol.strange.module.runes.Tier;

import javax.annotation.Nullable;
import java.util.*;

public class QuestHelper {
    public static final UUID ANY_UUID = UUID.fromString("0-0-0-0-0");
    public static final int MAX_QUESTS = 3;

    public static QuestException makeException(Quest quest, String message) {
        Quests.getQuestData().ifPresent(quests -> quests.remove(quest));
        return new QuestException(message);
    }

    public static Optional<Quest> getFirstSatisfiedQuest(Player player) {
        QuestData quests = Quests.getQuestData().orElseThrow();
        List<Quest> playerQuests = quests.all(player);
        return playerQuests.stream().filter(q -> q.isSatisfied(player)).findFirst();
    }

    public static void provideMap(ServerPlayer player, Quest quest, BlockPos pos, MapDecoration.Type type, int color) {
        var title = getTranslatedKey(quest.getDefinition(), "title");
        var map = MapHelper.create((ServerLevel) player.level, pos, title, type, color);
        player.getInventory().placeItemBackInInventory(map);
    }

    @Nullable
    public static QuestDefinition getDefinition(String id) {
        String[] split;
        String tierName;

        if (id.contains("/")) {
            id = id.replace("/", ".");
        }

        if (!id.contains(".")) {
            return null;
        }

        split = id.split("\\.");
        tierName = split[0];

        var tier = Tier.byName(tierName);
        if (tier == null) return null;

        if (Quests.DEFINITIONS.containsKey(tier) && Quests.DEFINITIONS.get(tier).containsKey(id)) {
            return Quests.DEFINITIONS.get(tier).get(id);
        }

        return null;
    }

    @Nullable
    public static QuestDefinition getRandomDefinition(ServerPlayer player, Tier tier, Random random) {
        UUID uuid = player.getUUID();

        if (!Quests.DEFINITIONS.containsKey(tier)) {
            LogHelper.warn(Quests.class, "No quest definitions available for this tier: " + tier);
            return null;
        }

        Map<String, QuestDefinition> definitions = Quests.DEFINITIONS.get(tier);
        if (definitions.isEmpty()) {
            LogHelper.warn(Quests.class, "No quests definitions found in this tier: " + tier);
            return null;
        }

        QuestData quests = Quests.getQuestData().orElseThrow();
        List<Quest> allPlayerQuests = quests.all(player);
        List<QuestDefinition> eligibleDefinitions = new ArrayList<>();
        List<QuestDefinition> tierDefinitions = new ArrayList<>(definitions.values());
        Collections.shuffle(tierDefinitions, random);
        QuestDefinition found = null;

        QUESTCHECK: for (QuestDefinition definition : tierDefinitions) {
            List<String> dimensions = definition.getDimensions();
            List<String> modules = definition.getModules();

            if (definition.isTest()) continue;

            if (!modules.isEmpty()) {
                Map<ResourceLocation, CharmModule> allModules = CommonLoader.getAllModules();
                for (String module : modules) {
                    ResourceLocation moduleId = new ResourceLocation(module);
                    if (!allModules.containsKey(moduleId) || !allModules.get(moduleId).isEnabled()) {
                        LogHelper.debug(Strange.MOD_ID, Quests.class, "Skipping definition " + definition.getId() + " because module dependency failed: " + moduleId);
                        break QUESTCHECK;
                    }
                }
            }

            if (!dimensions.isEmpty()) {
                ResourceLocation thisDimension = DimensionHelper.getDimension(player.level);
                List<ResourceLocation> dimensionIds = dimensions.stream().map(ResourceLocation::new).toList();
                if (!dimensionIds.contains(thisDimension)) {
                    LogHelper.debug(Strange.MOD_ID, Quests.class, "Skipping definition " + definition.getId() + " because dimension dependency failed: " + thisDimension);
                    break;
                }
            }

            // if the player is already doing this quest, add to eligible and skip
            if (allPlayerQuests.stream().anyMatch(q -> q.getDefinitionId().equals(definition.getId()))) {
                eligibleDefinitions.add(definition);
                continue;
            }

            // if the player has done this quest within the last 3 quests, add to eligible and skip
            if (Quests.LAST_QUESTS.containsKey(uuid)) {
                LinkedList<QuestDefinition> lastQuests = Quests.LAST_QUESTS.get(uuid);
                if (lastQuests.contains(definition)) {
                    eligibleDefinitions.add(definition);
                    continue;
                }
            }

            found = definition;
            break;
        }

        if (found == null && !eligibleDefinitions.isEmpty()) {
            LogHelper.debug(Strange.MOD_ID, Quests.class, "No exact quest definition found. Trying to using an eligible one instead");
            Collections.shuffle(eligibleDefinitions, random);
            found = eligibleDefinitions.get(0);
        }

        if (found == null) {
            LogHelper.debug(Strange.MOD_ID, Quests.class, "Could not find any eligible quest definitions");
        }

        return found;
    }

    /**
     * This is designed for server-side lookup of lang strings from a quest definition.
     * It uses the configured locale for lang lookups.
     *
     * Do not use this on the client; instead use {@link QuestsClient#getTranslatedKey}.
     * The client version queries the player's configured language.
     */
    public static Component getTranslatedKey(QuestDefinition definition, String key) {
        var id = definition.getId();
        var lang = definition.getLang();
        var code = Quests.locale;

        if (lang == null) return new TranslatableComponent(id);

        if (!lang.containsKey(code)) {
            code = Quests.DEFAULT_LOCALE;
        }

        var value = lang.get(code).getOrDefault(key, id);
        return new TranslatableComponent(value);
    }
}
