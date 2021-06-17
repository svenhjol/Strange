package svenhjol.strange.module.scrolls;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback.LootTableSetter;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.EntityKillCallback;
import svenhjol.charm.event.LoadServerFinishCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.mixin.accessor.MinecraftServerAccessor;
import svenhjol.charm.module.CharmModule;
import svenhjol.charm.module.bookcases.Bookcases;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.module.scrolls.tag.Quest;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@Module(mod = Strange.MOD_ID, client = ScrollsClient.class, description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends CharmModule {
    public static final int TIERS = 6;

    public static final ResourceLocation MSG_CLIENT_OPEN_SCROLL = new ResourceLocation(Strange.MOD_ID, "client_open_scroll"); // open the scroll screen with a populated quest
    public static final ResourceLocation MSG_CLIENT_SHOW_QUEST_TOAST = new ResourceLocation(Strange.MOD_ID, "client_show_quest_toast"); // trigger a toast on the client
    public static final ResourceLocation MSG_CLIENT_CACHE_CURRENT_QUESTS = new ResourceLocation(Strange.MOD_ID, "client_cache_current_quests"); // cache a list of all the player's quests
    public static final ResourceLocation MSG_CLIENT_DESTROY_SCROLL = new ResourceLocation(Strange.MOD_ID, "client_destroy_scroll"); // used for displaying particle effects
    public static final ResourceLocation MSG_SERVER_OPEN_SCROLL = new ResourceLocation(Strange.MOD_ID, "server_open_scroll"); // instruct server to fetch a quest (by id) and callback the client
    public static final ResourceLocation MSG_SERVER_FETCH_CURRENT_QUESTS = new ResourceLocation(Strange.MOD_ID, "server_fetch_current_quests"); // instruct server to fetch a list of all player's quests
    public static final ResourceLocation MSG_SERVER_ABANDON_QUEST = new ResourceLocation(Strange.MOD_ID, "server_abandon_quest"); // instruct server to abandon a quest (by id)

    public static final ResourceLocation TRIGGER_COMPLETED_SCROLL = new ResourceLocation(Strange.MOD_ID, "completed_scroll");
    public static final ResourceLocation SCROLL_LOOT_ID = new ResourceLocation(Strange.MOD_ID, "scroll_loot");
    public static LootItemFunctionType SCROLL_LOOT_FUNCTION;

    public static Map<Integer, Map<String, ScrollDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();
    public static Map<Integer, String> SCROLL_TIER_IDS = new HashMap<>();

    private static QuestManager questManager; // always access this via getQuestManager()

    @Config(name = "Use built-in scroll quests", description = "If true, scroll quests will use the built-in definitions. Use false to limit quests to datapacks.")
    public static boolean useBuiltInScrolls = true;

    @Config(name = "Add scrolls to loot", description = "If true, normal scrolls will be added to pillager loot and legendary scrolls to ancient rubble.")
    public static boolean addScrollsToLoot = true;

    @Config(name = "Scroll quest language", description = "The language key to use when displaying quest instructions.")
    public static String language = "en";

    @Config(name = "Exploration hint", description = "If true, the player who has an exploration quest will receive a visual and audible hint when reaching the location to start exploring.")
    public static boolean exploreHint = true;

    public Scrolls() {
        SCROLL_TIER_IDS.put(0, "test");
        SCROLL_TIER_IDS.put(1, "novice");
        SCROLL_TIER_IDS.put(2, "apprentice");
        SCROLL_TIER_IDS.put(3, "journeyman");
        SCROLL_TIER_IDS.put(4, "expert");
        SCROLL_TIER_IDS.put(5, "master");
        SCROLL_TIER_IDS.put(6, "legendary");
    }

    @Override
    public void register() {
        for (int tier = 0; tier <= TIERS; tier++) {
            SCROLL_TIERS.put(tier, new ScrollItem(this, tier, SCROLL_TIER_IDS.get(tier) + "_scroll"));
        }

        // handle adding normal scrolls to loot
        SCROLL_LOOT_FUNCTION = RegistryHelper.lootFunctionType(SCROLL_LOOT_ID, new LootItemFunctionType(new ScrollLootFunction.Serializer()));
    }

    @Override
    public void init() {
        // load quest manager and scrolls when world starts
        LoadServerFinishCallback.EVENT.register(server -> {
            loadQuestManager(server);
            tryLoadScrolls(server);
        });

        // handle entities being killed
        EntityKillCallback.EVENT.register(this::handleEntityDeath);

        // add scrolls to loot
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        // tick the quests belonging to the player
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        // tick the questmanager
        ServerTickEvents.END_SERVER_TICK.register(server -> questManager.tick());

        // allow scrolls on Charm's bookcases
        if (ModuleHandler.enabled(Bookcases.class)) {
            Bookcases.validItems.addAll(Scrolls.SCROLL_TIERS.values());
        }

        // handle incoming client packets
        ServerPlayNetworking.registerGlobalReceiver(Scrolls.MSG_SERVER_OPEN_SCROLL, this::handleServerOpenScroll);
        ServerPlayNetworking.registerGlobalReceiver(Scrolls.MSG_SERVER_FETCH_CURRENT_QUESTS, this::handleServerFetchCurrentQuests);
        ServerPlayNetworking.registerGlobalReceiver(Scrolls.MSG_SERVER_ABANDON_QUEST, this::handleServerAbandonQuest);
    }

    public static void sendPlayerQuestsPacket(ServerPlayer player) {
        Optional<QuestManager> questManager = getQuestManager();
        questManager.ifPresent(manager -> {
            sendQuestsPacket(player, manager.getQuests(player));
        });
    }

    public static void sendPlayerOpenScrollPacket(ServerPlayer player, Quest quest) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeNbt(quest.toTag());
        ServerPlayNetworking.send(player, MSG_CLIENT_OPEN_SCROLL, data);
    }

    public static void sendQuestsPacket(ServerPlayer player, List<Quest> quests) {
        // convert to nbt and write to packet buffer
        ListTag listTag = new ListTag();
        for (Quest quest : quests) {
            listTag.add(quest.toTag());
        }
        CompoundTag outTag = new CompoundTag();
        outTag.put("quests", listTag);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(outTag);

        ServerPlayNetworking.send(player, MSG_CLIENT_CACHE_CURRENT_QUESTS, buffer);
    }

    public static Optional<QuestManager> getQuestManager() {
        return questManager != null ? Optional.of(questManager) : Optional.empty();
    }

    @Nullable
    public static ScrollDefinition getRandomDefinition(int tier, Level world, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Charm.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        Map<String, ScrollDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        if (definitions.isEmpty()) {
            Charm.LOG.warn("No scroll definitions found in this tier: " + tier);
            return null;
        }

        ArrayList<ScrollDefinition> allDefinitions = new ArrayList<>(definitions.values());

        // try and fetch a random definition, checking the dimension restrictions of this scroll
        for (int tries = 0; tries < 10; tries++) {
            ScrollDefinition definition = allDefinitions.get(random.nextInt(definitions.size()));
            List<String> validDimensions = definition.getValidDimensions();

            if (validDimensions.isEmpty())
                return definition;

            for (String validDimension : validDimensions) {
                if (DimensionHelper.isDimension(world, new ResourceLocation(validDimension)))
                    return definition;
            }
        }

        return null;
    }

    @Nullable
    public static ScrollDefinition getDefinition(String definition) {
        String[] split;
        String tierName;

        if (definition.contains("/"))
            definition = definition.replace("/", ".");

        if (!definition.contains("."))
            return null;

        split = definition.split("\\.");
        if (split.length == 3) {
            // full form, used by quest populators
            tierName = split[1];
        } else {
            // short-hand form, used in commands
            tierName = split[0];
            definition = "scrolls." + definition;
        }

        for (Map.Entry<Integer, String> entry : SCROLL_TIER_IDS.entrySet()) {
            int scrollTierNum = entry.getKey();
            String scrollTierName = entry.getValue();

            if (tierName.equals(scrollTierName) && AVAILABLE_SCROLLS.containsKey(scrollTierNum))
                return AVAILABLE_SCROLLS.get(scrollTierNum).getOrDefault(definition, null);
        }

        return null;
    }

    private void loadQuestManager(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            Charm.LOG.warn("[Scrolls] Overworld is null, cannot load persistent state manager");
            return;
        }

        DimensionDataStorage stateManager = overworld.getDataStorage();
        questManager = stateManager.computeIfAbsent(
            (tag) -> QuestManager.fromNbt(overworld, tag),
            () -> new QuestManager(overworld),
            QuestManager.nameFor(overworld.dimensionType()));

        Charm.LOG.info("[Scrolls] Loaded quest state manager");
    }

    private void tryLoadScrolls(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getResources().getResourceManager();

        for (int tier = 0; tier <= TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new HashMap<>());
            Collection<ResourceLocation> scrolls = resources.listResources("scrolls/" + SCROLL_TIER_IDS.get(tier), file -> file.endsWith(".json"));

            for (ResourceLocation scroll : scrolls) {
                try {
                    ScrollDefinition definition = ScrollDefinition.deserialize(resources.getResource(scroll));

                    // check that scroll definition is built-in and configured to be used
                    if (definition.isDefaultPack() && !useBuiltInScrolls)
                        continue;

                    // check that scroll modules are present and enabled
                    List<String> requiredModules = definition.getModules();
                    if (!requiredModules.isEmpty()) {
                        boolean valid = true;
                        for (String requiredModule : requiredModules) {
                            valid = valid && ModuleHandler.enabled(requiredModule);
                        }
                        if (!valid) {
                            Charm.LOG.info("Scroll definition " + scroll.toString() + " is missing required modules, disabling.");
                            continue;
                        }
                    }

                    String id = scroll.getPath().replace("/", ".").replace(".json", "");
                    definition.setId(id);
                    definition.setTitle(id);
                    definition.setTier(tier);
                    AVAILABLE_SCROLLS.get(tier).put(id, definition);
                    Charm.LOG.info("Loaded scroll definition " + scroll.toString() + " for tier " + tier);

                } catch (Exception e) {
                    Charm.LOG.warn("Could not load scroll definition for " + scroll.toString() + " because " + e.getMessage());
                }
            }
        }
    }

    private void handleEntityDeath(LivingEntity entity, DamageSource source) {
        Entity attacker = source.getEntity();
        if (!Scrolls.getQuestManager().isPresent())
            return;

        Scrolls.getQuestManager().get()
            .forEachQuest(quest -> quest.entityKilled(entity, attacker));
    }

    private void handlePlayerTick(Player player) {
        if (player.level.getGameTime() % 20 != 0)
            return; // poll once every second

        if (!(player instanceof ServerPlayer))
            return; // must be server-side

        if (!Scrolls.getQuestManager().isPresent())
            return; // must have an instantiated quest manager

        ServerPlayer serverPlayer = (ServerPlayer) player;
        Scrolls.getQuestManager().get()
            .forEachPlayerQuest(serverPlayer, quest -> quest.playerTick(serverPlayer));
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableSetter setter) {
        if (!addScrollsToLoot)
            return;

        if (id.equals(BuiltInLootTables.PILLAGER_OUTPOST) || id.equals(BuiltInLootTables.WOODLAND_MANSION)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new ScrollLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }

        if (id.equals(StrangeLoot.RUBBLE)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(UniformGenerator.between(0.0F, 1.0F))
                .with(LootItem.lootTableItem(SCROLL_TIERS.get(TIERS)));

            supplier.withPool(builder);
        }
    }

    private void handleServerOpenScroll(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        String questId = data.readUtf(16);
        if (questId == null || questId.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            Optional<Quest> optionalQuest = manager.getQuest(questId);
            if (!optionalQuest.isPresent())
                return;

            Scrolls.sendPlayerOpenScrollPacket(player, optionalQuest.get());
        });
    }

    private void handleServerFetchCurrentQuests(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        processClientPacket(server, player, manager -> Scrolls.sendPlayerQuestsPacket(player));
    }

    private void handleServerAbandonQuest(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        String questId = data.readUtf(16);
        if (questId == null || questId.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            Optional<Quest> optionalQuest = manager.getQuest(questId);
            if (!optionalQuest.isPresent())
                return;

            Quest quest = optionalQuest.get();
            quest.abandon(player);

            Scrolls.sendPlayerQuestsPacket(player);
        });
    }

    private void processClientPacket(MinecraftServer server, ServerPlayer player, Consumer<QuestManager> callback) {
        server.execute(() -> {
            if (player == null)
                return;

            Optional<QuestManager> questManager = Scrolls.getQuestManager();
            questManager.ifPresent(callback);
        });
    }

    public static void triggerCompletedScroll(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_COMPLETED_SCROLL);
    }
}
