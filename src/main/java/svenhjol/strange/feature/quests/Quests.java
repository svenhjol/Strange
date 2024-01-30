package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charmony.api.event.*;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.event.QuestEvents;
import svenhjol.strange.feature.quests.QuestsNetwork.*;
import svenhjol.strange.feature.quests.reward.RewardItemFunctions;
import svenhjol.strange.feature.travel_journal.TravelJournal;
import svenhjol.strange.helper.DataHelper;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Quests extends CommonFeature {
    public static final String QUESTS_TAG = "quests";
    static final List<QuestDefinition> DEFINITIONS = new ArrayList<>();
    private static final Map<UUID, QuestList> PLAYER_QUESTS = new HashMap<>();
    private static final Map<UUID, QuestList> VILLAGER_QUESTS = new HashMap<>();
    private static final Map<UUID, Long> VILLAGER_QUESTS_REFRESH = new HashMap<>();
    private static final Map<UUID, Integer> VILLAGER_LOYALTY = new HashMap<>();

    static Supplier<LootItemFunctionType> lootFunction;
    static Supplier<SoundEvent> abandonSound;
    static Supplier<SoundEvent> acceptSound;
    static Supplier<SoundEvent> completeSound;
    static Supplier<SoundEvent> epicCompleteSound;

    public static final ResourceLocation DEFAULT_REWARD_FUNCTIONS = new ResourceLocation(Strange.ID, "default_reward_functions");
    public static final RewardItemFunctions REWARD_ITEM_FUNCTIONS = new RewardItemFunctions();

    public static int maxQuestRequirements = 4;
    public static int maxQuestRewards = 5;
    public static int maxPlayerQuests = 3;
    public static int maxVillagerQuests = 3;

    @Override
    public void register() {
        var registry = mod().registry();

        QuestsNetwork.register(registry);

        lootFunction = registry.lootFunctionType("quest",
            () -> new LootItemFunctionType(QuestLootFunction.CODEC));

        abandonSound = registry.soundEvent("quest_abandon");
        acceptSound = registry.soundEvent("quest_accept");
        completeSound = registry.soundEvent("quest_complete");
        epicCompleteSound = registry.soundEvent("quest_epic_complete");
    }

    @Override
    public void runWhenEnabled() {
        ServerStartEvent.INSTANCE.handle(this::handleServerStart);
        EntityKilledEvent.INSTANCE.handle(this::handleEntityKilled);
        EntityLeaveEvent.INSTANCE.handle(this::handleEntityLeave);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
        LootTableModifyEvent.INSTANCE.handle(this::handleLootTableModify);

        TravelJournal.registerPlayerDataSource(
            (player, tag) -> PLAYER_QUESTS.put(player.getUUID(), QuestList.load(tag.getCompound(QUESTS_TAG))),
            (player, tag) -> tag.put(QUESTS_TAG, getPlayerQuests(player).save()));

        TravelJournal.registerSyncHandler(Quests::syncQuests);
    }

    private Optional<LootPool.Builder> handleLootTableModify(LootDataManager manager, ResourceLocation id) {
        var builder = LootPool.lootPool();

        builder.setRolls(ConstantValue.exactly(1));
        builder.add(LootItem.lootTableItem(Items.AIR)
            .setWeight(1)
            .apply(() -> new QuestLootFunction(id)));

        return Optional.of(builder);
    }

    private void handleEntityKilled(LivingEntity entity, DamageSource source) {
        if (source.getEntity() instanceof Player player && !player.level().isClientSide) {
            getPlayerQuests(player).all().forEach(q -> q.entityKilled(entity, source));
        }
    }

    private void handleEntityLeave(Entity entity, Level level) {
        getAllPlayerQuests().forEach(q -> q.entityLeave(entity));
    }

    private void handlePlayerTick(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<Quest> invalid = new ArrayList<>();
            for (var quest : getPlayerQuests(serverPlayer).all()) {
                quest.tick(serverPlayer);

                // If any quest became invalidated during its tick then remove it
                if (!quest.inProgress()) {
                    invalid.add(quest);
                }

                // If any quest needs to be updated on the client then sync
                if (quest.isDirty()) {
                    quest.setDirty(false);
                    // TODO: efficiency - handle single quest update rather than all of them
                    syncQuests(serverPlayer);
                }
            }

            if (!invalid.isEmpty()) {
                invalid.forEach(q -> removeQuest(serverPlayer, q));
            }
        }
    }

    public static void syncQuests(ServerPlayer player) {
        SyncPlayerQuests.send(player, getPlayerQuests(player));
    }

    public static void addQuest(ServerPlayer player, Quest quest) {
        PLAYER_QUESTS.computeIfAbsent(player.getUUID(), a -> new QuestList())
            .add(quest);

        quest.player = player;
        syncQuests(player);
    }

    public static void increaseLoyalty(UUID villagerUuid, int amount) {
        var current = getLoyalty(villagerUuid);
        VILLAGER_LOYALTY.put(villagerUuid, current + amount);
    }

    public static void resetLoyalty(UUID villagerUuid) {
        VILLAGER_LOYALTY.put(villagerUuid, 0);
    }

    public static int getLoyalty(UUID villagerUuid) {
        return VILLAGER_LOYALTY.getOrDefault(villagerUuid, 0);
    }

    public static void removeQuest(ServerPlayer player, Quest quest) {
        PLAYER_QUESTS.computeIfAbsent(player.getUUID(), a -> new QuestList())
            .remove(quest);

        syncQuests(player);
    }

    public static void setPlayerQuests(Player player, QuestList quests) {
        PLAYER_QUESTS.put(player.getUUID(), quests);
    }

    public static void setVillagerQuests(UUID villagerUuid, QuestList quests) {
        VILLAGER_QUESTS.put(villagerUuid, quests);
    }

    public static QuestList getPlayerQuests(Player player) {
        return PLAYER_QUESTS.getOrDefault(player.getUUID(), new QuestList());
    }

    public static QuestList getVillagerQuests(UUID villagerUuid) {
        return VILLAGER_QUESTS.getOrDefault(villagerUuid, new QuestList());
    }

    public static List<Quest> getPlayerQuests(QuestType type) {
        return getAllPlayerQuests().stream()
            .filter(q -> q.type().equals(type))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Quest> getAllPlayerQuests() {
        List<Quest> quests = new ArrayList<>();
        PLAYER_QUESTS.values().forEach(questList -> quests.addAll(questList.all()));
        return quests;
    }

    public static Optional<Quest> getPlayerQuest(Player player, String questId) {
        return getPlayerQuests(player).get(questId);
    }

    private void handleServerStart(MinecraftServer server) {
        DEFINITIONS.clear();

        var manager = server.getResourceManager();
        for (var tier : QuestsHelper.TIERS.values()) {
            var resources = manager.listResources("quests/definition/" + tier, file -> file.getPath().endsWith(".json"));
            for (var entry : resources.entrySet()) {
                var id = entry.getKey();
                var resource = entry.getValue();
                var definition = QuestDefinition.deserialize(id.getNamespace(), resource);

                // If the def requires specific charmony features, test they are enabled now and skip def if not.
                if (!DataHelper.hasRequiredFeatures(definition.requiredFeatures())) {
                    mod().log().debug(getClass(), "Definition " + id + " has missing or disabled features, skipping");
                    continue;
                }

                DEFINITIONS.add(definition);
                mod().log().debug(getClass(), "Registered quest definition " + id);
            }
        }
    }

    public static void handleRequestVillagerQuests(RequestVillagerQuests message, Player player) {
        var ticksToRefresh = 80; // TODO: test value, refresh quests every 4 seconds
        var level = player.level();
        var random = level.getRandom();
        var gameTime = level.getGameTime();
        var villagerUuid = message.getVillagerUuid();
        var serverPlayer = (ServerPlayer)player;

        // Is villager nearby?
        var nearby = QuestsHelper.getNearbyMatchingVillager(level, player.blockPosition(), villagerUuid);
        if (nearby.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.NO_VILLAGER);
            return;
        }

        var villager = nearby.get();
        var villagerData = villager.getVillagerData();
        var villagerProfession = villagerData.getProfession();
        var villagerLevel = villagerData.getLevel();
        var lastRefresh = VILLAGER_QUESTS_REFRESH.get(villagerUuid);
        var quests = VILLAGER_QUESTS.getOrDefault(villagerUuid, new QuestList());

        if (lastRefresh != null && gameTime - lastRefresh < ticksToRefresh) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
            SyncVillagerQuests.send(serverPlayer, quests, villagerUuid, villagerProfession);
            return;
        }

        var definitions = QuestsHelper.makeDefinitions(villagerUuid, villagerProfession, 1, villagerLevel, maxVillagerQuests, random);
        if (definitions.isEmpty()) {
            NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.NO_QUESTS_GENERATED);
            return;
        }

        var server = serverPlayer.level().getServer();
        if (server == null) {
            throw new RuntimeException("Could not get server reference");
        }

        var manager = server.getResourceManager();
        var newQuests = QuestsHelper.makeQuestsFromDefinitions(manager, definitions, villagerUuid);

        VILLAGER_QUESTS.put(villagerUuid, newQuests);
        VILLAGER_QUESTS_REFRESH.put(villagerUuid, gameTime);

        NotifyVillagerQuestsResult.send(serverPlayer, VillagerQuestsResult.SUCCESS);
        SyncVillagerQuests.send(serverPlayer, newQuests, villagerUuid, villagerProfession);
    }

    public static void handleRequestPlayerQuests(RequestPlayerQuests message, Player player) {
        syncQuests((ServerPlayer)player);
    }

    public static void handleAcceptQuest(AcceptQuest message, Player player) {
        var level = player.level();
        var questId = message.getQuestId();
        var villagerUuid = message.getVillagerUuid();
        var serverPlayer = (ServerPlayer)player;

        // Player at max quests?
        if (QuestsHelper.hasMaxQuests(player)) {
            NotifyAcceptQuestResult.send(serverPlayer, null, AcceptQuestResult.MAX_QUESTS);
            return;
        }

        // Player already on this quest?
        if (getPlayerQuest(serverPlayer, questId).isPresent()) {
            NotifyAcceptQuestResult.send(serverPlayer, null, AcceptQuestResult.ALREADY_ON_QUEST);
            return;
        }

        // Is villager nearby?
        var nearby = QuestsHelper.getNearbyMatchingVillager(level, player.blockPosition(), villagerUuid);
        if (nearby.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, null, AcceptQuestResult.NO_VILLAGER);
            return;
        }

        // Check that the villager has quests.
        var villagerQuests = VILLAGER_QUESTS.get(villagerUuid);
        if (villagerQuests == null || villagerQuests.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, null, AcceptQuestResult.VILLAGER_HAS_NO_QUESTS);
            return;
        }

        // Check that the villager quest ID is valid.
        var opt = villagerQuests.all().stream().filter(q -> q.id().equals(questId)).findFirst();
        if (opt.isEmpty()) {
            NotifyAcceptQuestResult.send(serverPlayer, null, AcceptQuestResult.NO_QUEST);
            return;
        }

        // Remove this quest from the villager quests.
        villagerQuests.remove(questId);
        VILLAGER_QUESTS.put(villagerUuid, villagerQuests);

        level.playSound(null, serverPlayer.blockPosition(), acceptSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // When the player accepts an epic, reset the villager's loyalty.
        resetLoyalty(villagerUuid);

        // Add this quest to the player's quests.
        var quest = opt.get();
        quest.start(serverPlayer);
        addQuest(serverPlayer, quest);

        // Fire the AcceptQuestEvent on the server side.
        QuestEvents.ACCEPT_QUEST.invoke(player, quest);

        // Update the client with the result.
        NotifyAcceptQuestResult.send(serverPlayer, quest.id(), AcceptQuestResult.SUCCESS);
    }

    public static void handleAbandonQuest(AbandonQuest message, Player player) {
        var serverPlayer = (ServerPlayer)player;
        var quests = getPlayerQuests(player);
        var questId = message.getQuestId();

        // Player even has quests?
        if (quests.isEmpty()) {
            NotifyAbandonQuestResult.send(serverPlayer, null, AbandonQuestResult.NO_QUESTS);
            return;
        }

        // Player has this quest?
        var opt = quests.get(questId);
        if (opt.isEmpty()) {
            NotifyAbandonQuestResult.send(serverPlayer, null, AbandonQuestResult.NO_QUEST);
            return;
        }

        var quest = opt.get();
        quest.cancel();

        serverPlayer.level().playSound(null, serverPlayer.blockPosition(), abandonSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // Fire the AbandonQuestEvent on the server side.
        QuestEvents.ABANDON_QUEST.invoke(player, quest);

        // Remove this quest from the player's quests.
        removeQuest(serverPlayer, quest);

        // Update the client with the result.
        NotifyAbandonQuestResult.send(serverPlayer, quest.id(), AbandonQuestResult.SUCCESS);
    }

    /**
     * Called by mixin when a player interacts with a villager.
     * If any player quests can be completed by the villager then run that action here.
     */
    public static boolean tryComplete(ServerPlayer player, Villager villager) {
        var quests = getPlayerQuests(player);
        var satisfied = quests.all().stream().filter(Quest::satisfied).toList();
        if (satisfied.isEmpty()) return false;

        var matchesQuestGiver = satisfied.stream().filter(q -> q.villagerUuid().equals(villager.getUUID())).toList();
        if (!matchesQuestGiver.isEmpty()) {
            for (Quest quest : matchesQuestGiver) {
                completeWithVillager(player, villager, quest);
            }
            return true;
        }

        var matchesProfession = satisfied.stream().filter(q -> q.villagerProfession().equals(villager.getVillagerData().getProfession())).toList();
        if (!matchesProfession.isEmpty()) {
            for (Quest quest : matchesProfession) {
                quest.villagerUuid = villager.getUUID();
                completeWithVillager(player, villager, quest);
            }
            return true;
        }

        return false;
    }

    private static void completeWithVillager(ServerPlayer player, Villager villager, Quest quest) {
        var level = (ServerLevel)player.level();
        var pos = player.blockPosition();
        var villagerUuid = villager.getUUID();

        level.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (quest.isEpic()) {
            level.playSound(null, pos, epicCompleteSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            level.playSound(null, pos, completeSound.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        quest.complete();
        removeQuest(player, quest);
        increaseLoyalty(villagerUuid, 1);
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

    public enum AbandonQuestResult {
        NO_QUESTS,
        NO_QUEST,
        SUCCESS
    }
}
