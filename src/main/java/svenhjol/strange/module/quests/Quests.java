package svenhjol.strange.module.quests;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.event.QuestEvents;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge.Tier;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Quests extends CharmModule {
    public static final ResourceLocation MSG_SERVER_SYNC_PLAYER_QUESTS = new ResourceLocation(Strange.MOD_ID, "server_sync_player_quests");
    public static final ResourceLocation MSG_SERVER_ABANDON_QUEST = new ResourceLocation(Strange.MOD_ID, "server_abandon_quest");
    public static final ResourceLocation MSG_SERVER_PAUSE_QUEST = new ResourceLocation(Strange.MOD_ID, "server_pause_quest");
    public static final ResourceLocation MSG_CLIENT_SHOW_QUEST_TOAST = new ResourceLocation(Strange.MOD_ID, "client_show_quest_toast");
    public static final ResourceLocation MSG_CLIENT_SYNC_PLAYER_QUESTS = new ResourceLocation(Strange.MOD_ID, "client_sync_player_quests");

    public static final int NUM_TIERS = 6;
    public static final String DEFINITION_FOLDER = "quest_definitions";
    public static final Map<Integer, Map<String, QuestDefinition>> DEFINITIONS = new HashMap<>();
    public static final Map<Integer, String> TIER_NAMES;
    public static final Map<UUID, LinkedList<QuestDefinition>> LAST_QUESTS = new HashMap<>();

    private static QuestData quests;

    @Override
    public void runWhenEnabled() {
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_SYNC_PLAYER_QUESTS, this::handleSyncPlayerQuests);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_ABANDON_QUEST, this::handleServerAbandonQuest);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_PAUSE_QUEST, this::handleServerPauseQuest);

        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::handleKilledEntity);
        QuestEvents.START.register(this::handleStartQuest);
        QuestEvents.COMPLETE.register(this::handleCompleteQuest);
        QuestEvents.ABANDON.register(this::handleAbandonQuest);
        QuestEvents.REMOVE.register(this::handleRemoveQuest);

        QuestCommand.init();
    }

    public static void sendToast(ServerPlayer player, QuestToastType type, String definitionId, int tier) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeEnum(type);
        buffer.writeUtf(definitionId);
        buffer.writeInt(tier);
        ServerPlayNetworking.send(player, Quests.MSG_CLIENT_SHOW_QUEST_TOAST, buffer);
    }

    public static Optional<QuestData> getQuestData() {
        return Optional.ofNullable(quests);
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
        int tier = getTierByName(tierName);

        if (DEFINITIONS.containsKey(tier) && DEFINITIONS.get(tier).containsKey(id)) {
            return DEFINITIONS.get(tier).get(id);
        }

        return null;
    }

    @Nullable
    public static QuestDefinition getRandomDefinition(ServerPlayer player, int tier, Random random) {
        UUID uuid = player.getUUID();

        if (!DEFINITIONS.containsKey(tier)) {
            LogHelper.warn(Quests.class, "No quest definitions available for this tier: " + tier);
            return null;
        }

        Map<String, QuestDefinition> definitions = DEFINITIONS.get(tier);
        if (definitions.isEmpty()) {
            LogHelper.warn(Quests.class, "No quests definitions found in this tier: " + tier);
            return null;
        }

        QuestData quests = getQuestData().orElseThrow();
        List<Quest> allPlayerQuests = quests.getAll(player);
        List<QuestDefinition> eligibleDefinitions = new ArrayList<>();
        List<QuestDefinition> tierDefinitions = new ArrayList<>(definitions.values());
        Collections.shuffle(tierDefinitions, random);
        QuestDefinition found = null;

        QUESTCHECK: for (QuestDefinition definition : tierDefinitions) {
            List<String> dimensions = definition.getDimensions();
            List<String> modules = definition.getModules();

            if (!modules.isEmpty()) {
                Map<ResourceLocation, CharmModule> allModules = CommonLoader.getAllModules();
                for (String module : modules) {
                    ResourceLocation moduleId = new ResourceLocation(module);
                    if (!allModules.containsKey(moduleId) || !allModules.get(moduleId).isEnabled()) {
                        LogHelper.debug(Quests.class, "Skipping definition " + definition.getId() + " because module dependency failed: " + moduleId);
                        break QUESTCHECK;
                    }
                }
            }

            if (!dimensions.isEmpty()) {
                ResourceLocation thisDimension = DimensionHelper.getDimension(player.level);
                List<ResourceLocation> dimensionIds = dimensions.stream().map(ResourceLocation::new).collect(Collectors.toList());
                if (!dimensionIds.contains(thisDimension)) {
                    LogHelper.debug(Quests.class, "Skipping definition " + definition.getId() + " because dimension dependency failed: " + thisDimension);
                    break;
                }
            }

            // if the player is already doing this quest, add to eligible and skip
            if (allPlayerQuests.stream().anyMatch(q -> q.getDefinitionId().equals(definition.getId()))) {
                eligibleDefinitions.add(definition);
                continue;
            }

            // if the player has done this quest within the last 3 quests, add to eligible and skip
            if (LAST_QUESTS.containsKey(uuid)) {
                LinkedList<QuestDefinition> lastQuests = LAST_QUESTS.get(uuid);
                if (lastQuests.contains(definition)) {
                    eligibleDefinitions.add(definition);
                    continue;
                }
            }

            found = definition;
            break;
        }

        if (found == null && !eligibleDefinitions.isEmpty()) {
            LogHelper.debug(Quests.class, "No exact quest definition found. Trying to using an eligible one instead");
            Collections.shuffle(eligibleDefinitions, random);
            found = eligibleDefinitions.get(0);
        }

        if (found == null) {
            LogHelper.debug(Quests.class, "Could not find any eligible quest definitions");
        }

        return found;
    }

    public static int getTierByName(String name) {
        for (Map.Entry<Integer, String> tier : TIER_NAMES.entrySet()) {
            if (tier.getValue().equals(name)) {
                return tier.getKey();
            }
        }
        return 0;
    }

    private void handleKilledEntity(ServerLevel level, Entity attacker, LivingEntity target) {
        getQuestData().ifPresent(quests -> quests.eachQuest(q -> q.entityKilled(target, attacker)));
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            ResourceManager manager = server.getResourceManager();
            Map<ResourceLocation, CharmModule> allModules = CommonLoader.getAllModules();

            for (int tier = 0; tier < NUM_TIERS; tier++) {
                Collection<ResourceLocation> definitions = manager.listResources(DEFINITION_FOLDER + "/" + TIER_NAMES.get(tier), file -> file.endsWith(".json"));
                LogHelper.debug(this.getClass(), "Tier " + tier + " has " + definitions.size() + " definitions");
                for (ResourceLocation resource : definitions) {
                    try {
                        QuestDefinition definition = QuestDefinition.deserialize(manager.getResource(resource));

                        // check definition module requirements
                        List<String> modules = definition.getModules();
                        if (!modules.isEmpty()) {
                            boolean skip = false;
                            for (String module : modules) {
                                ResourceLocation modres = new ResourceLocation(module);
                                if (!allModules.containsKey(modres)) {
                                    LogHelper.info(this.getClass(), "Quest definition " + definition.getId() + " requires module " + modres + ", skipping");
                                    skip = true;
                                    break;
                                }
                            }

                            if (skip) continue;
                        }

                        String id = resource.getPath()
                            .replace(DEFINITION_FOLDER, "")
                            .replace("/", ".")
                            .replace(".json", "");

                        if (id.startsWith(".")) {
                            id = id.substring(1);
                        }

                        definition.setId(id);
                        definition.setTier(tier);

                        DEFINITIONS.computeIfAbsent(tier, a -> new HashMap<>()).put(id, definition);
                        String path = resource.getPath();
                        String fileName = path.substring(path.lastIndexOf("/") + 1);
                        LogHelper.debug(this.getClass(), "Loaded tier " + tier + " definition: " + fileName);
                    } catch (Exception e) {
                        LogHelper.warn(this.getClass(), "Could not load quest definition for " + resource.toString() + ": " + e.getMessage());
                    }
                }
            }

            // setup the data storage
            ServerLevel serverLevel = (ServerLevel)level;
            DimensionDataStorage storage = serverLevel.getDataStorage();

            quests = storage.computeIfAbsent(
                nbt -> QuestData.fromNbt(serverLevel, nbt),
                () -> new QuestData(serverLevel),
                QuestData.getFileId(serverLevel.getLevel().dimensionType()));

            LogHelper.info(this.getClass(), "Loaded quests saved data");
        }
    }

    private void handleSyncPlayerQuests(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        syncPlayerQuests(player);
    }

    /**
     * When a quest is removed (from the master quest data state):
     * - synchronise all player quest state back to the client
     */
    private void handleRemoveQuest(Quest quest, ServerPlayer player) {
        syncPlayerQuests(player);
    }

    /**
     * When a quest is started:
     * - log to the last_quest list so that the same quest doesn't get recommended straight away
     * - send toast to the player
     * - synchronise all player quest state back to the client
     */
    private void handleStartQuest(Quest quest, ServerPlayer player) {
        LinkedList<QuestDefinition> definitions = LAST_QUESTS.computeIfAbsent(player.getUUID(), a -> new LinkedList<>());
        if (definitions.size() >= 3) {
            definitions.pop();
        }
        definitions.push(quest.getDefinition());
        Quests.sendToast(player, QuestToast.QuestToastType.STARTED, quest.getDefinitionId(), quest.getTier());
        syncPlayerQuests(player);
    }

    /**
     * When a quest is abandoned:
     * - send toast to the player
     */
    private void handleAbandonQuest(Quest quest, ServerPlayer player) {
        Quests.sendToast(player, QuestToast.QuestToastType.ABANDONED, quest.getDefinitionId(), quest.getTier());
    }

    /**
     * When a quest is completed:
     * - send toast to the player
     */
    private void handleCompleteQuest(Quest quest, ServerPlayer player) {
        Quests.sendToast(player, QuestToast.QuestToastType.COMPLETED, quest.getDefinitionId(), quest.getTier());
    }

    private void handleServerAbandonQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        String questId = buffer.readUtf();
        server.execute(() -> Quests.getQuestData().flatMap(quests -> quests.get(questId)).ifPresent(quest -> {
            quest.abandon(player);
            Journals.sendOpenJournal(player, Journals.Page.QUESTS);
        }));
    }

    private void handleServerPauseQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        String questId = buffer.readUtf();
        server.execute(() -> Quests.getQuestData().flatMap(quests -> quests.get(questId)).ifPresent(quest -> {
            quest.pause(player);
            Journals.sendOpenJournal(player, Journals.Page.QUESTS);
        }));
    }

    private void syncPlayerQuests(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        quests.saveForPlayer(player, tag);
        NetworkHelper.sendPacketToClient(player, MSG_CLIENT_SYNC_PLAYER_QUESTS, buf -> buf.writeNbt(tag));
    }

    static {
        TIER_NAMES = new HashMap<>();
        TIER_NAMES.put(0, Tier.TEST.getSerializedName());
        TIER_NAMES.put(1, Tier.NOVICE.getSerializedName());
        TIER_NAMES.put(2, Tier.APPRENTICE.getSerializedName());
        TIER_NAMES.put(3, Tier.JOURNEYMAN.getSerializedName());
        TIER_NAMES.put(4, Tier.EXPERT.getSerializedName());
        TIER_NAMES.put(5, Tier.MASTER.getSerializedName());
    }
}
