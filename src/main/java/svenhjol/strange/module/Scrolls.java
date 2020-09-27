package svenhjol.strange.module;

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
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.event.EntityDeathCallback;
import svenhjol.meson.event.LoadWorldCallback;
import svenhjol.meson.event.PlayerTickCallback;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.client.ScrollsClient;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.mixin.accessor.MinecraftServerAccessor;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.loot.ScrollLootFunction;
import svenhjol.strange.scroll.tag.QuestTag;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

@Module(description = "Scrolls provide quest instructions and scrollkeeper villagers give rewards for completed scrolls.")
public class Scrolls extends MesonModule {
    public static final int MAX_TIERS = 6;
    public static final Identifier SCROLL_LOOT_ID = new Identifier(Strange.MOD_ID, "scroll_loot");
    public static final Identifier MSG_CLIENT_OPEN_SCROLL = new Identifier(Strange.MOD_ID, "client_open_scroll");
    public static LootFunctionType SCROLL_LOOT_FUNCTION;
    public static Map<Integer, List<JsonDefinition>> AVAILABLE_SCROLLS = new HashMap<>();
    public static Map<Integer, ScrollItem> SCROLL_TIERS = new HashMap<>();
    public static Map<Integer, String> SCROLL_TIER_IDS = new HashMap<>();

    public ScrollsClient client;

    @Config(name = "Use build-in scroll quests", description = "If true, scroll quests will use the built-in definitions. Use false to limit quests to datapacks.")
    public static boolean useBuiltInScrolls = true;

    @Config(name = "Add scrolls to loot", description = "If true, scrolls will be added to dungeon loot chests.")
    public static boolean addScrollsToLoot = true;

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
            SCROLL_TIERS.put(tier, new ScrollItem(this, tier, "scroll_" + SCROLL_TIER_IDS.get(tier)));
        }

        SCROLL_LOOT_FUNCTION = new LootFunctionType(new ScrollLootFunction.Serializer());
        Registry.register(Registry.LOOT_FUNCTION_TYPE, SCROLL_LOOT_ID, SCROLL_LOOT_FUNCTION);
    }

    @Override
    public void init() {
        // load the scroll definitions when the world loads
        LoadWorldCallback.EVENT.register(this::tryLoadScrolls);

        // handle entities being killed
        EntityDeathCallback.EVENT.register(this::handleEntityDeath);

        // add scrolls to loot
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        // check player inventory and callback quest methods
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
    }

    @Override
    public void clientInit() {
        this.client = new ScrollsClient(this);
    }

    private void tryLoadScrolls(MinecraftServer server) {
        ResourceManager resources = ((MinecraftServerAccessor)server).getServerResourceManager().getResourceManager();

        for (int tier = 1; tier <= MAX_TIERS; tier++) {
            AVAILABLE_SCROLLS.put(tier, new ArrayList<>());
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
                            valid = valid && Meson.enabled(requiredModule);
                        }
                        if (!valid) {
                            Meson.LOG.info("Scroll definition " + scroll.toString() + " is missing required modules, disabling.");
                            continue;
                        }
                    }

                    String name = scroll.getPath().replace("/", ".").replace(".json", "");
                    definition.setTitle(name);
                    definition.setTier(tier);
                    AVAILABLE_SCROLLS.get(tier).add(definition);
                    Meson.LOG.info("Loaded scroll definition " + scroll.toString() + " for tier " + tier);

                } catch (Exception e) {
                    Meson.LOG.warn("Could not load scroll definition for " + scroll.toString() + " because " + e.getMessage());
                }
            }
        }
    }

    private void handleEntityDeath(LivingEntity entity, DamageSource source) {
        Entity attacker = source.getAttacker();
        if (!(attacker instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity)attacker;

        forEachQuest(player, (scroll, quest) -> {
            quest.getHunt().playerKilledEntity(player, entity);

            if (quest.isDirty()) {
                quest.markDirty(false);
                ScrollItem.setScrollQuest(scroll, quest);
            }
        });
    }

    private void handlePlayerTick(PlayerEntity player) {
        if (player.world.getTime() % 20 != 0)
            return; // poll every second

        forEachQuest(player, (scroll, quest) -> {
            quest.getExplore().inventoryTick(player);

            if (quest.isDirty()) {
                quest.markDirty(false);
                ScrollItem.setScrollQuest(scroll, quest);
            }
        });
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableSetter setter) {
        if (!addScrollsToLoot)
            return;

        if (id.equals(LootTables.SIMPLE_DUNGEON_CHEST)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.PAPER)
                    .weight(1)
                    .apply(() -> new ScrollLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }

    private void forEachQuest(PlayerEntity player, BiConsumer<ItemStack, QuestTag> callback) {
        player.inventory.main.forEach(stack -> {
            if (!(stack.getItem() instanceof ScrollItem))
                return;

            QuestTag quest = ScrollItem.getScrollQuest(stack);
            if (quest == null)
                return;

            callback.accept(stack, quest);
        });
    }

    @Nullable
    public static JsonDefinition getRandomDefinition(int tier, Random random) {
        if (!Scrolls.AVAILABLE_SCROLLS.containsKey(tier)) {
            Meson.LOG.warn("No scroll definitions available for this tier: " + tier);
            return null;
        }

        List<JsonDefinition> definitions = AVAILABLE_SCROLLS.get(tier);
        if (definitions.isEmpty()) {
            Meson.LOG.warn("No scroll definitions found in this tier: " + tier);
            return null;
        }

        return definitions.get(random.nextInt(definitions.size()));
    }

    public static List<ItemStack> getAllPlayerScrolls(PlayerEntity player) {
        List<ItemStack> scrolls = new ArrayList<>();

        player.inventory.main.forEach(stack -> {
            if (stack.isEmpty())
                return;

            if (stack.getItem() instanceof ScrollItem) {
                scrolls.add(stack);
            }
        });

        return scrolls;
    }
}
