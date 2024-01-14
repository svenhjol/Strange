package svenhjol.strange.feature.quests;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charmony.api.event.EntityJoinEvent;
import svenhjol.charmony.api.event.PlayerTickEvent;
import svenhjol.charmony.api.event.ServerStartEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.QuestsNetwork.*;

import java.util.*;
import java.util.stream.Collectors;

public class Quests extends CommonFeature {
    static final List<IQuestDefinition> DEFINITIONS = new ArrayList<>();
    public static final Map<UUID, List<Quest<?>>> PLAYER_QUESTS = new HashMap<>();
    public static final Map<UUID, List<Quest<?>>> VILLAGER_QUESTS = new HashMap<>();
    public static final Map<UUID, Long> VILLAGER_QUESTS_REFRESH = new HashMap<>();

    public static int maxPlayerQuests = 3;
    public static int maxVillagerQuests = 3;

    @Override
    public void register() {
        QuestDefinitions.init();
        QuestsNetwork.register(mod().registry());
    }

    @Override
    public void runWhenEnabled() {
        ServerStartEvent.INSTANCE.handle(this::handleServerStart);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
    }

    private void handlePlayerTick(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var uuid = serverPlayer.getUUID();
            if (PLAYER_QUESTS.containsKey(uuid)) {
                for (Quest<?> quest : PLAYER_QUESTS.get(uuid)) {
                    quest.tick(serverPlayer);
                }
            }
        }
    }

    public static void registerDefinition(IQuestDefinition definition) {
        Mods.common(Strange.ID).log().debug(Quests.class, "Registering definition " + definition);
        DEFINITIONS.add(definition);
    }

    public static void syncQuests(ServerPlayer player) {
        SyncPlayerQuests.send(player, PLAYER_QUESTS.getOrDefault(player.getUUID(), List.of()));
    }

    public static void addQuest(ServerPlayer player, Quest<?> quest) {
        PLAYER_QUESTS.computeIfAbsent(player.getUUID(), a -> new ArrayList<>())
            .add(quest);

        quest.player = player;
    }

    public static void removeQuest(ServerPlayer player, Quest<?> quest) {
        PLAYER_QUESTS.computeIfAbsent(player.getUUID(), a -> new ArrayList<>())
            .remove(quest);
    }

    public static Optional<Quest<?>> getQuest(ServerPlayer player, String questId) {
        return PLAYER_QUESTS.getOrDefault(player.getUUID(), List.of())
            .stream().filter(q -> q.id().equals(questId))
            .findFirst();
    }

    private void handleServerStart(MinecraftServer server) {
        PLAYER_QUESTS.clear();
    }

    private void handleEntityJoin(Entity entity, Level level) {
        if (entity instanceof ServerPlayer player) {
            syncQuests(player);
        }
    }

    public static void handleRequestVillagerQuests(RequestVillagerQuests message, Player player) {
        var level = player.level();
        var random = level.getRandom();
        var gameTime = level.getGameTime();
        var villagerUuid = message.getVillagerUuid();
        var serverPlayer = (ServerPlayer)player;

        // Is villager nearby?
        var nearby = QuestHelper.getNearbyVillager(level, player.blockPosition(), villagerUuid);
        if (nearby.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.NO_VILLAGER);
            return;
        }

        var villager = nearby.get();
        var villagerData = villager.getVillagerData();
        var villagerProfession = villagerData.getProfession();
        var villagerLevel = villagerData.getLevel();
        var lastRefresh = VILLAGER_QUESTS_REFRESH.get(villagerUuid);
        var quests = VILLAGER_QUESTS.getOrDefault(villagerUuid, new ArrayList<>());

        if (lastRefresh != null && gameTime - lastRefresh < 80) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
            SyncVillagerQuests.send(serverPlayer, quests, villagerUuid, villagerProfession);
            return;
        }

        // Generate new quests for this villager
        quests.clear();

        var definitions = QuestHelper.makeDefinitionsForVillager(villagerProfession, 1, villagerLevel, maxVillagerQuests, random);
        if (definitions.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.NO_QUESTS_GENERATED);
            return;
        }

        var newQuests = QuestHelper.makeQuestsFromDefinitions(definitions, villagerUuid);

        VILLAGER_QUESTS.put(villagerUuid, newQuests);
        VILLAGER_QUESTS_REFRESH.put(villagerUuid, gameTime);

        NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
        SyncVillagerQuests.send(serverPlayer, newQuests, villagerUuid, villagerProfession);
    }

    public static void handleAcceptQuest(QuestsNetwork.AcceptQuest message, Player player) {
        var level = player.level();
        var questId = message.getQuestId();
        var villagerUuid = message.getVillagerUuid();
        var serverPlayer = (ServerPlayer)player;

        // Player at max quests?
        if (QuestHelper.hasMaxQuests(player)) {
            NotifyAcceptQuestResult.send(serverPlayer, AcceptQuestResult.MAX_QUESTS);
            return;
        }

        // Player already on this quest?
        if (getQuest(serverPlayer, questId).isPresent()) {
            NotifyAcceptQuestResult.send(serverPlayer, AcceptQuestResult.ALREADY_ON_QUEST);
            return;
        }

        // Is villager nearby?
        var nearby = QuestHelper.getNearbyVillager(level, player.blockPosition(), villagerUuid);
        if (nearby.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, AcceptQuestResult.NO_VILLAGER);
            return;
        }

        // Check that the villager has quests.
        var villagerQuests = VILLAGER_QUESTS.get(villagerUuid);
        if (villagerQuests == null || villagerQuests.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, AcceptQuestResult.VILLAGER_HAS_NO_QUESTS);
            return;
        }

        // Check that the villager quest ID is valid.
        var opt = villagerQuests.stream().filter(q -> q.id().equals(questId)).findFirst();
        if (opt.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, AcceptQuestResult.NO_QUEST);
            return;
        }

        // Remove this quest from the villager quests.
        VILLAGER_QUESTS.put(villagerUuid, villagerQuests.stream().filter(q -> !q.id().equals(questId))
            .collect(Collectors.toCollection(ArrayList::new)));

        // Add this quest to the player's quests.
        var quest = opt.get();
        addQuest(serverPlayer, quest);
    }

    public enum VillagerQuestsResult {
        NO_VILLAGER,
        NO_QUESTS_GENERATED,
        EMPTY,
        SUCCESS
    }

    public enum AcceptQuestResult {
        NO_VILLAGER,
        NO_QUEST,
        MAX_QUESTS,
        ALREADY_ON_QUEST,
        VILLAGER_HAS_NO_QUESTS,
        SUCCESS
    }
}
