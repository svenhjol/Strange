package svenhjol.strange.feature.quests;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import svenhjol.charmony.api.event.EntityJoinEvent;
import svenhjol.charmony.api.event.PlayerTickEvent;
import svenhjol.charmony.api.event.ServerStartEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.QuestsNetwork.NotifyVillagerQuestsResult;
import svenhjol.strange.feature.quests.QuestsNetwork.RequestVillagerQuests;
import svenhjol.strange.feature.quests.QuestsNetwork.SyncPlayerQuests;
import svenhjol.strange.feature.quests.QuestsNetwork.SyncVillagerQuests;

import java.util.*;

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
    }

    public static void removeQuest(ServerPlayer player, Quest<?> quest) {
        PLAYER_QUESTS.computeIfAbsent(player.getUUID(), a -> new ArrayList<>())
            .remove(quest);
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
        var nearby = level.getEntitiesOfClass(Villager.class, new AABB(player.blockPosition()).inflate(4.0d));
        var opt = nearby.stream().filter(e -> e.getUUID().equals(villagerUuid)).findFirst();
        if (opt.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.EMPTY);
            return;
        }

        var villager = opt.get();
        var villagerData = villager.getVillagerData();
        var lastRefresh = VILLAGER_QUESTS_REFRESH.get(villagerUuid);
        var quests = VILLAGER_QUESTS.getOrDefault(villagerUuid, new ArrayList<>());

        if (lastRefresh != null && lastRefresh - gameTime < 24000) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
            SyncVillagerQuests.send(serverPlayer, quests, villagerUuid);
            return;
        }

        // Generate new quests for this villager
        quests.clear();

        var definitions = QuestHelper.getDefinitionsUpToLevel(villagerData.getProfession(), villagerData.getLevel(), 5, random);
        if (definitions.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.EMPTY);
            return;
        }

        var newQuests = QuestHelper.makeQuests(definitions);

        VILLAGER_QUESTS.put(villagerUuid, newQuests);
        VILLAGER_QUESTS_REFRESH.put(villagerUuid, gameTime);

        NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
        SyncVillagerQuests.send(serverPlayer, newQuests, villagerUuid);
    }

    public enum VillagerQuestsResult {
        EMPTY,
        SUCCESS
    }
}
