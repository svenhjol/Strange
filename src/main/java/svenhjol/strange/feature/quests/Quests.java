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
import svenhjol.strange.feature.quests.QuestsNetwork.SyncQuests;

import java.util.*;

public class Quests extends CommonFeature {
    static final List<IQuestDefinition> DEFINITIONS = new ArrayList<>();
    static final Map<UUID, List<Quest<?>>> PLAYER_QUESTS = new HashMap<>();

    public static int maxQuests = 3;

    @Override
    public void register() {
        QuestDefinitions.init();
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
        SyncQuests.send(player, PLAYER_QUESTS.getOrDefault(player.getUUID(), List.of()));
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
}
