package svenhjol.strange.module.quests;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.api.event.PlayerTickCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.loader.CommonLoader;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;
import svenhjol.strange.module.quests.command.QuestCommand;
import svenhjol.strange.module.quests.command.arg.QuestDefinitionArgType;
import svenhjol.strange.module.quests.command.arg.QuestIdArgType;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.api.event.QuestEvents;
import svenhjol.strange.module.quests.network.*;
import svenhjol.strange.module.runes.Tier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, description = "Quests are player tasks with rewards provided by a Scrollkeeper villager.")
public class Quests extends CharmModule {
    public static final String DEFINITION_FOLDER = "quests";
    public static final Map<Tier, Map<String, QuestDefinition>> DEFINITIONS = new HashMap<>();
    public static final Map<UUID, LinkedList<QuestDefinition>> LAST_QUESTS = new HashMap<>();
    public static final String DEFAULT_LOCALE = "en";

    public static ServerSendQuests SERVER_SEND_QUESTS;
    public static ServerSendQuestDefinitions SERVER_SEND_QUEST_DEFINITIONS;
    public static ServerSendQuestToast SERVER_SEND_QUEST_TOAST;
    public static ServerReceiveAbandonQuest SERVER_RECEIVE_ABANDON_QUEST;
    public static ServerReceivePauseQuest SERVER_RECEIVE_PAUSE_QUEST;

    private static @Nullable QuestData quests;

    public static boolean rewardRunes = true;
    public static boolean showExplorePlacement = false; // TODO: must be FALSE for production!
    public static boolean showExploreHint = true;

    @Override
    public void register() {
        ArgumentTypes.register("quest_definition", QuestDefinitionArgType.class, new EmptyArgumentSerializer<>(QuestDefinitionArgType::new));
        ArgumentTypes.register("quest_id", QuestIdArgType.class, new EmptyArgumentSerializer<>(QuestIdArgType::new));
    }

    @Override
    public void runWhenEnabled() {
        SERVER_SEND_QUESTS = new ServerSendQuests();
        SERVER_SEND_QUEST_DEFINITIONS = new ServerSendQuestDefinitions();
        SERVER_SEND_QUEST_TOAST = new ServerSendQuestToast();
        SERVER_RECEIVE_ABANDON_QUEST = new ServerReceiveAbandonQuest();
        SERVER_RECEIVE_PAUSE_QUEST = new ServerReceivePauseQuest();

        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::handleKilledEntity);

        QuestEvents.START.register(this::handleStartQuest);
        QuestEvents.COMPLETE.register(this::handleCompleteQuest);
        QuestEvents.ABANDON.register(this::handleAbandonQuest);
        QuestEvents.REMOVE.register(this::handleRemoveQuest);
        QuestEvents.PAUSE.register(this::handlePauseQuest);

        QuestCommand.init();
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

        var tier = Tier.byName(tierName);
        if (tier == null) return null;

        if (DEFINITIONS.containsKey(tier) && DEFINITIONS.get(tier).containsKey(id)) {
            return DEFINITIONS.get(tier).get(id);
        }

        return null;
    }

    @Nullable
    public static QuestDefinition getRandomDefinition(ServerPlayer player, Tier tier, Random random) {
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
                List<ResourceLocation> dimensionIds = dimensions.stream().map(ResourceLocation::new).collect(Collectors.toList());
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
            LogHelper.debug(Strange.MOD_ID, Quests.class, "No exact quest definition found. Trying to using an eligible one instead");
            Collections.shuffle(eligibleDefinitions, random);
            found = eligibleDefinitions.get(0);
        }

        if (found == null) {
            LogHelper.debug(Strange.MOD_ID, Quests.class, "Could not find any eligible quest definitions");
        }

        return found;
    }

    private void handleKilledEntity(ServerLevel level, Entity attacker, LivingEntity target) {
        getQuestData().ifPresent(quests -> quests.eachQuest(q -> q.entityKilled(target, attacker)));
    }

    private void handlePlayerTick(Player player) {
        getQuestData().ifPresent(quests -> quests.eachQuest(q -> q.playerTick(player)));
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        SERVER_SEND_QUEST_DEFINITIONS.send(player);
        SERVER_SEND_QUESTS.send(player);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            ResourceManager manager = server.getResourceManager();
            Map<ResourceLocation, CharmModule> allModules = CommonLoader.getAllModules();

            for (Tier tier : Tier.values()) {
                Collection<ResourceLocation> definitions = manager.listResources(DEFINITION_FOLDER + "/" + tier.getSerializedName(), file -> file.endsWith(".json"));
                LogHelper.debug(Strange.MOD_ID, this.getClass(), "Tier " + tier + " has " + definitions.size() + " definitions");
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
                                    LogHelper.info(Strange.MOD_ID, this.getClass(), "Quest definition " + definition.getId() + " requires module " + modres + ", skipping");
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
                        LogHelper.debug(Strange.MOD_ID, this.getClass(), "Loaded tier " + tier + " definition: " + fileName);
                    } catch (Exception e) {
                        LogHelper.warn(this.getClass(), "Could not load quest definition for " + resource.toString() + ": " + e.getMessage());
                    }
                }
            }

            // setup the data storage
            ServerLevel overworld = (ServerLevel)level;
            DimensionDataStorage storage = overworld.getDataStorage();

            quests = storage.computeIfAbsent(
                tag -> QuestData.load(overworld, tag),
                () -> new QuestData(overworld),
                QuestData.getFileId(overworld.getLevel().dimensionType()));

            LogHelper.info(Strange.MOD_ID, this.getClass(), "Loaded quests saved data");
        }
    }

    /**
     * Runs when a quest is started.
     *
     * At this point the quest has been added to the SavedData.
     * The quest is a reference to the live object.
     *
     * The new quest's definition is stored against the player so that the next time
     * they start a quest we can try and avoid giving them the same quest again.
     *
     * Update all quests on the client and send the toast.
     */
    private void handleStartQuest(Quest quest, ServerPlayer player) {
        LinkedList<QuestDefinition> definitions = LAST_QUESTS.computeIfAbsent(player.getUUID(), a -> new LinkedList<>());
        if (definitions.size() >= 3) {
            definitions.pop();
        }
        definitions.push(quest.getDefinition());

        SERVER_SEND_QUESTS.send(player);
        SERVER_SEND_QUEST_TOAST.send(player, quest, QuestToastType.STARTED);
    }

    /**
     * Runs when a quest is abandoned.
     *
     * At this point the quest has been removed from the SavedData
     * and the quest object is a clone of the original.
     *
     * Update all quests on the client and send the toast.
     */
    private void handleAbandonQuest(Quest quest, ServerPlayer player) {
        SERVER_SEND_QUESTS.send(player);
        SERVER_SEND_QUEST_TOAST.send(player, quest, QuestToastType.ABANDONED);
    }

    /**
     * Runs when a quest is paused.
     *
     * At this point the quest has been removed from the SavedData
     * and the quest object is a clone of the original.
     *
     * Update all quests on the client but don't send a toast.
     */
    private void handlePauseQuest(Quest quest, ServerPlayer player) {
        SERVER_SEND_QUESTS.send(player);
    }

    /**
     * Runs when a quest is completed.
     *
     * At this point the quest has been removed from the SavedData
     * and the quest object is a clone of the original.
     *
     * Update all quests on the client and send the toast.
     */
    private void handleCompleteQuest(Quest quest, ServerPlayer player) {
        SERVER_SEND_QUESTS.send(player);
        SERVER_SEND_QUEST_TOAST.send(player, quest, QuestToastType.COMPLETED);
    }

    /**
     * Runs when a quest is removed.
     *
     * At this point the quest has been removed from the SavedData
     * and the quest object is a clone of the original.
     *
     * We update the client's quests but don't send a toast.
     */
    private void handleRemoveQuest(Quest quest, ServerPlayer player) {
        SERVER_SEND_QUESTS.send(player);
    }
}
