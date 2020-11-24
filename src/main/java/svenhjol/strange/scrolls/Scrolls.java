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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
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
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.EntityDeathCallback;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.*;

@Module(mod = Strange.MOD_ID, client = ScrollsClient.class, description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends CharmModule {
    public static final int MAX_TIERS = 6;
    public static final Identifier SCROLL_LOOT_ID = new Identifier(Strange.MOD_ID, "scroll_loot");

    public static final Identifier MSG_CLIENT_OPEN_SCROLL = new Identifier(Strange.MOD_ID, "client_open_scroll");
    public static final Identifier MSG_CLIENT_QUEST_TOAST = new Identifier(Strange.MOD_ID, "client_quest_toast");

    public static LootFunctionType SCROLL_LOOT_FUNCTION;
    public static Map<Integer, Map<String, JsonDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();
    public static Map<Integer, String> SCROLL_TIER_IDS = new HashMap<>();

    public static QuestManager questManager;

    @Config(name = "Use built-in scroll quests", description = "If true, scroll quests will use the built-in definitions. Use false to limit quests to datapacks.")
    public static boolean useBuiltInScrolls = true;

    @Config(name = "Add scrolls to loot", description = "If true, scrolls will be added to dungeon loot chests.")
    public static boolean addScrollsToLoot = true;

    @Config(name = "Loot chance", description = "Chance (out of 1.0) of a scroll appearing in dungeon or pillager loot.")
    public static double lootChance = 0.25F;

    @Config(name = "Scroll quest language", description = "The language key to use when displaying quest instructions.")
    public static String language = "en";

    public Scrolls() {
        SCROLL_TIER_IDS.put(1, "novice");
        SCROLL_TIER_IDS.put(2, "apprentice");
        SCROLL_TIER_IDS.put(3, "journeyman");
        SCROLL_TIER_IDS.put(4, "expert");
        SCROLL_TIER_IDS.put(5, "master");
        SCROLL_TIER_IDS.put(6, "legendary");
    }

    @Override
    public void register() {
        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            SCROLL_TIERS.put(tier, new ScrollItem(this, tier, SCROLL_TIER_IDS.get(tier) + "_scroll"));
        }

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

        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new HashMap<>());
            Collection<Identifier> scrolls = resources.findResources("scrolls/" + SCROLL_TIER_IDS.get(tier), file -> file.endsWith(".json"));

            for (Identifier scroll : scrolls) {
                try {
                    JsonDefinition definition = JsonDefinition.deserialize(resources.getResource(scroll));

                    // check that scroll definition is built-in and configured to be used
                    if (definition.isBuiltIn() && !useBuiltInScrolls)
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
        if (!(attacker instanceof ServerPlayerEntity))
            return;

        ServerPlayerEntity player = (ServerPlayerEntity) attacker;
        Scrolls.questManager.forEachQuest(quest -> quest.playerKilledEntity(player, entity));

//        forEachQuest(player, (scroll, quest) -> {
//            quest.playerKilledEntity(player, entity);
//
//            if (quest.isDirty()) {
//                quest.setDirty(false);
//                ScrollItem.setScrollQuest(scroll, quest);
//            }
//        });
    }

    private void handlePlayerTick(PlayerEntity player) {
        if (player.world.getTime() % 20 != 0)
            return; // poll once every second

        if (!(player instanceof ServerPlayerEntity))
            return; // must be server-side

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Scrolls.questManager.forEachPlayerQuest(serverPlayer, quest -> quest.playerTick(serverPlayer));

//        forEachQuest(player, (scroll, quest) -> {
//            quest.playerTick(player);
//
//            if (quest.isDirty()) {
//                quest.setDirty(false);
//                ScrollItem.setScrollQuest(scroll, quest);
//            }
//        });
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableSetter setter) {
        if (!addScrollsToLoot)
            return;

        if (id.equals(LootTables.SIMPLE_DUNGEON_CHEST)
            || id.equals(LootTables.PILLAGER_OUTPOST_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new ScrollLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }

    public static Optional<QuestManager> getQuestManager() {
        return Optional.of(questManager);
    }

    @Nullable
    public static JsonDefinition getRandomDefinition(int tier, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Charm.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        Map<String, JsonDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        if (definitions.isEmpty()) {
            Charm.LOG.warn("No scroll definitions found in this tier: " + tier);
            return null;
        }

        return new ArrayList<>(definitions.values()).get(random.nextInt(definitions.size()));
    }

    @Nullable
    public static JsonDefinition getDefinition(String definition) {
        if (!definition.contains("."))
            return null;

        String[] split = definition.split("\\.");

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

    public static List<ItemStack> getAllPlayerScrolls(PlayerEntity player) {
        List<ItemStack> scrolls = new ArrayList<>();

        player.inventory.main.forEach(stack -> {
            if (stack.isEmpty())
                return;

            if (stack.getItem() instanceof ScrollItem)
                scrolls.add(stack);
        });

        return scrolls;
    }
}
