package svenhjol.strange.scrolls;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback.LootTableSetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDeathCallback;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, client = ScrollsClient.class, description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends CharmModule {
    public static final int TIERS = 6;

    public static final Identifier MSG_CLIENT_OPEN_SCROLL = new Identifier(Strange.MOD_ID, "client_open_scroll"); // open the scroll screen with a populated quest
    public static final Identifier MSG_CLIENT_SHOW_QUEST_TOAST = new Identifier(Strange.MOD_ID, "client_show_quest_toast"); // trigger a toast on the client
    public static final Identifier MSG_CLIENT_CACHE_CURRENT_QUESTS = new Identifier(Strange.MOD_ID, "client_cache_current_quests"); // cache a list of all the player's quests
    public static final Identifier MSG_CLIENT_DESTROY_SCROLL = new Identifier(Strange.MOD_ID, "client_destroy_scroll"); // used for displaying particle effects
    public static final Identifier MSG_SERVER_OPEN_SCROLL = new Identifier(Strange.MOD_ID, "server_open_scroll"); // instruct server to fetch a quest (by id) and callback the client
    public static final Identifier MSG_SERVER_FETCH_CURRENT_QUESTS = new Identifier(Strange.MOD_ID, "server_fetch_current_quests"); // instruct server to fetch a list of all player's quests
    public static final Identifier MSG_SERVER_ABANDON_QUEST = new Identifier(Strange.MOD_ID, "server_abandon_quest"); // instruct server to abandon a quest (by id)

    public static final Identifier SCROLL_LOOT_ID = new Identifier(Strange.MOD_ID, "scroll_loot");
    public static LootFunctionType SCROLL_LOOT_FUNCTION;

    public static Map<Integer, Map<String, JsonDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();
    public static Map<Integer, String> SCROLL_TIER_IDS = new HashMap<>();

    private static QuestManager questManager; // always access this via the optional: getQuestManager()

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
        SCROLL_LOOT_FUNCTION = RegistryHandler.lootFunctionType(SCROLL_LOOT_ID, new LootFunctionType(new ScrollLootFunction.Serializer()));
    }

    @Override
    public void init() {
        // load quest manager and scrolls when world starts
        LoadWorldCallback.EVENT.register(server -> {
            loadQuestManager(server);
            tryLoadScrolls(server);
        });

        // handle entities being killed
        EntityDeathCallback.EVENT.register(this::handleEntityDeath);

        // add scrolls to loot
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        // tick the quests belonging to the player
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);

        // tick the questmanager
        ServerTickEvents.END_SERVER_TICK.register(server -> questManager.tick());

        ScrollsServer server = new ScrollsServer();
        server.init();
    }

    private void loadQuestManager(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            Charm.LOG.warn("Overworld is null, cannot load persistent state manager");
            return;
        }

        PersistentStateManager stateManager = overworld.getPersistentStateManager();
        questManager = stateManager.getOrCreate(() -> new QuestManager(overworld), QuestManager.nameFor(overworld.getDimension()));
    }

    private void tryLoadScrolls(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getServerResourceManager().getResourceManager();

        for (int tier = 0; tier <= TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new HashMap<>());
            Collection<Identifier> scrolls = resources.findResources("scrolls/" + SCROLL_TIER_IDS.get(tier), file -> file.endsWith(".json"));

            for (Identifier scroll : scrolls) {
                try {
                    JsonDefinition definition = JsonDefinition.deserialize(resources.getResource(scroll));

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
        Entity attacker = source.getAttacker();
        if (!Scrolls.getQuestManager().isPresent())
            return;

        Scrolls.getQuestManager().get()
            .forEachQuest(quest -> quest.entityKilled(entity, attacker));
    }

    private void handlePlayerTick(PlayerEntity player) {
        if (player.world.getTime() % 20 != 0)
            return; // poll once every second

        if (!(player instanceof ServerPlayerEntity))
            return; // must be server-side

        if (!Scrolls.getQuestManager().isPresent())
            return; // must have an instantiated quest manager

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Scrolls.getQuestManager().get()
            .forEachPlayerQuest(serverPlayer, quest -> quest.playerTick(serverPlayer));
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableSetter setter) {
        if (!addScrollsToLoot)
            return;

        if (id.equals(LootTables.PILLAGER_OUTPOST_CHEST) || id.equals(LootTables.WOODLAND_MANSION_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new ScrollLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }

        if (id.equals(StrangeLoot.ANCIENT_RUBBLE)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(UniformLootTableRange.between(0.0F, 1.0F))
                .with(ItemEntry.builder(SCROLL_TIERS.get(TIERS)));

            supplier.pool(builder);
        }
    }

    public static Optional<QuestManager> getQuestManager() {
        return questManager != null ? Optional.of(questManager) : Optional.empty();
    }

    @Nullable
    public static JsonDefinition getRandomDefinition(int tier, World world, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Charm.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        Map<String, JsonDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        if (definitions.isEmpty()) {
            Charm.LOG.warn("No scroll definitions found in this tier: " + tier);
            return null;
        }

        ArrayList<JsonDefinition> allDefinitions = new ArrayList<>(definitions.values());

        // try and fetch a random definition, checking the dimension restrictions of this scroll
        for (int tries = 0; tries < 10; tries++) {
            JsonDefinition definition = allDefinitions.get(random.nextInt(definitions.size()));
            List<String> validDimensions = definition.getValidDimensions();

            if (validDimensions.isEmpty())
                return definition;

            for (String validDimension : validDimensions) {
                if (DimensionHelper.isDimension(world, new Identifier(validDimension)))
                    return definition;
            }
        }

        return null;
    }

    @Nullable
    public static JsonDefinition getDefinition(String definition) {
        String[] split;

        if (definition.contains("/"))
            definition = definition.replace("/", ".");

        if (!definition.contains("."))
            return null;

        split = definition.split("\\.");

        for (Map.Entry<Integer, String> entry : SCROLL_TIER_IDS.entrySet()) {
            int tierNum = entry.getKey();
            String tierName = entry.getValue();

            if (tierName.equals(split[0])) {
                if (AVAILABLE_SCROLLS.containsKey(tierNum)) {
                    return AVAILABLE_SCROLLS.get(tierNum).getOrDefault("scrolls." + definition, null);
                }
            }
        }

        return null;
    }
}
