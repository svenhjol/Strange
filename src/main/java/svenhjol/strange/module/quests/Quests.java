package svenhjol.strange.module.quests;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
import svenhjol.strange.api.network.QuestMessages;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.quests.QuestToast.QuestToastType;
import svenhjol.strange.module.quests.command.QuestCommand;
import svenhjol.strange.module.quests.command.arg.QuestDefinitionArgType;
import svenhjol.strange.module.quests.command.arg.QuestIdArgType;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.event.QuestEvents;
import svenhjol.strange.module.runes.Tier;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Quests extends CharmModule {
    public static final String DEFINITION_FOLDER = "quests";
    public static final Map<Tier, Map<String, QuestDefinition>> DEFINITIONS = new HashMap<>();
    public static final Map<UUID, LinkedList<QuestDefinition>> LAST_QUESTS = new HashMap<>();

    private static QuestData quests;

    public static boolean rewardRunes = true;

    @Override
    public void register() {
        ArgumentTypes.register("quest_definition", QuestDefinitionArgType.class, new EmptyArgumentSerializer<>(QuestDefinitionArgType::new));
        ArgumentTypes.register("quest_id", QuestIdArgType.class, new EmptyArgumentSerializer<>(QuestIdArgType::new));
    }

    @Override
    public void runWhenEnabled() {
        ServerPlayNetworking.registerGlobalReceiver(QuestMessages.SERVER_SYNC_QUESTS, this::handleSyncQuests);
        ServerPlayNetworking.registerGlobalReceiver(QuestMessages.SERVER_ABANDON_QUEST, this::handleAbandonQuest);
        ServerPlayNetworking.registerGlobalReceiver(QuestMessages.SERVER_PAUSE_QUEST, this::handlePauseQuest);

        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::handleKilledEntity);

        QuestEvents.START.register(this::handleStartQuest);
        QuestEvents.COMPLETE.register(this::handleCompleteQuest);
        QuestEvents.ABANDON.register(this::handleAbandonQuest);
        QuestEvents.REMOVE.register(this::handleRemoveQuest);

        QuestCommand.init();
    }

    public static void sendToast(ServerPlayer player, QuestToastType type, String definitionId, Tier tier) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeEnum(type);
        buffer.writeUtf(definitionId);
        buffer.writeInt(tier.getLevel());
        ServerPlayNetworking.send(player, QuestMessages.CLIENT_SHOW_QUEST_TOAST, buffer);
    }

    public static void sendDefinitions(ServerPlayer player) {
        var tag = new CompoundTag();
        int count = 0;

        for (Map.Entry<Tier, Map<String, QuestDefinition>> entry : DEFINITIONS.entrySet()) {
            var tier = entry.getKey();
            var tag1 = new CompoundTag();

            for (Map.Entry<String, QuestDefinition> tierEntry : entry.getValue().entrySet()) {
                var id = tierEntry.getKey();
                var definition = tierEntry.getValue();

                count++;
                tag1.put(id, definition.save());
            }

            tag.put(tier.getSerializedName(), tag1);
        }

        LogHelper.debug(Quests.class, "Sending " + count + " quest definitions to " + player.getUUID());
        NetworkHelper.sendPacketToClient(player, QuestMessages.CLIENT_SYNC_QUEST_DEFINITIONS, buf -> buf.writeNbt(tag));
    }

    public static void sendQuests(ServerPlayer player) {
        var tag = quests.save(player);
        NetworkHelper.sendPacketToClient(player, QuestMessages.CLIENT_SYNC_QUESTS, buf -> buf.writeNbt(tag));
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

    private void handleKilledEntity(ServerLevel level, Entity attacker, LivingEntity target) {
        getQuestData().ifPresent(quests -> quests.eachQuest(q -> q.entityKilled(target, attacker)));
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        sendDefinitions(player);
        sendQuests(player);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            ResourceManager manager = server.getResourceManager();
            Map<ResourceLocation, CharmModule> allModules = CommonLoader.getAllModules();

            for (Tier tier : Tier.values()) {
                Collection<ResourceLocation> definitions = manager.listResources(DEFINITION_FOLDER + "/" + tier.getSerializedName(), file -> file.endsWith(".json"));
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
            ServerLevel overworld = (ServerLevel)level;
            DimensionDataStorage storage = overworld.getDataStorage();

            quests = storage.computeIfAbsent(
                tag -> QuestData.load(overworld, tag),
                () -> new QuestData(overworld),
                QuestData.getFileId(overworld.getLevel().dimensionType()));

            LogHelper.info(this.getClass(), "Loaded quests saved data");
        }
    }

    private void handleSyncQuests(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendQuests(player));
    }

    /**
     * When a quest is removed (from the master quest data state):
     * - synchronise all player quest state back to the client
     */
    private void handleRemoveQuest(Quest quest, ServerPlayer player) {
        sendQuests(player);
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
        sendQuests(player);
    }

    /**
     * When a quest is abandoned:
     * - send toast to the player
     */
    private void handleAbandonQuest(Quest quest, ServerPlayer player) {
        Quests.sendToast(player, QuestToast.QuestToastType.ABANDONED, quest.getDefinitionId(), quest.getTier());
        sendQuests(player);
    }

    /**
     * When a quest is completed:
     * - send toast to the player
     */
    private void handleCompleteQuest(Quest quest, ServerPlayer player) {
        Quests.sendToast(player, QuestToast.QuestToastType.COMPLETED, quest.getDefinitionId(), quest.getTier());
        sendQuests(player);
    }

    private void handleAbandonQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        String questId = buffer.readUtf();
        var quests = Quests.getQuestData().orElse(null);
        if (quests == null) return;

        server.execute(() -> {
            var quest = quests.get(questId);
            if (quest != null) {
                quest.abandon(player);
                Journals.sendOpenPage(player, PageTracker.Page.QUESTS);
            }
        });
    }

    private void handlePauseQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        String questId = buffer.readUtf();
        var quests = Quests.getQuestData().orElse(null);
        if (quests == null) return;

        server.execute(() -> {
            var quest = quests.get(questId);
            if (quest != null) {
                quest.pause(player);
                Journals.sendOpenPage(player, PageTracker.Page.QUESTS);
            }
        });
    }
}
