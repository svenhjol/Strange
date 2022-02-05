package svenhjol.strange.module.relics;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.event.PlayerTickCallback;
import svenhjol.charm.helper.ClassHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NbtHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.relics.loot.RelicLootFunction;
import svenhjol.strange.module.stone_ruins.StoneRuins;
import svenhjol.strange.module.stone_ruins.StoneRuinsLoot;
import svenhjol.strange.module.vaults.Vaults;
import svenhjol.strange.module.vaults.VaultsLoot;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID, description = "Relics are items, weapons and armor with enchantments beyond normal limits.\n" +
    "They may also contain 'impossible' enchantment combinations.")
public class Relics extends CharmModule {
    public static final String ITEM_NAMESPACE = "svenhjol.strange.module.relics.item";
    public static final String RELIC_TAG = "strange_relic";
    public static final Map<Type, List<IRelicItem>> RELICS = new HashMap<>();

    public static final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "tool_relic_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    private static final ResourceLocation TRIGGER_FIND_RELIC = new ResourceLocation(Strange.MOD_ID, "find_relic");
    private static Advancement CACHED_RELIC_ADVANCEMENT = null;

    @Config(name = "Additional levels", description = "Number of levels above the enchantment maximum level that can be applied to relics.\n" +
        "Enchantment levels are capped at level 10.")
    public static int extraLevels = 5;

    @Config(name = "Additional loot tables", description = "List of additional loot tables that relics will be added to.")
    public static List<String> additionalLootTables = List.of();

    @Config(name = "Blacklist", description = "List of relic items that will not be loaded. See wiki for details.")
    public static List<String> configBlacklist = new ArrayList<>();

    @Override
    public void register() {
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new RelicLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        var vaultsEnabled = Strange.LOADER.isEnabled(Vaults.class);
        var stoneRuinsEnabled = Strange.LOADER.isEnabled(StoneRuins.class);

        // This adds the tools, weapons and armor to the vaults large room. "Weird" relics will be handled by rubble later.
        var defaultLootTable = vaultsEnabled ? VaultsLoot.VAULTS_LARGE_ROOM : (stoneRuinsEnabled ? StoneRuinsLoot.STONE_RUINS_ROOM : BuiltInLootTables.WOODLAND_MANSION);
        VALID_LOOT_TABLES.add(defaultLootTable);

        try {
            List<String> classes = ClassHelper.getClassesInPackage(ITEM_NAMESPACE);
            for (var className : classes) {
                var simpleClassName = className.substring(className.lastIndexOf(".") + 1);
                try {
                    var clazz = Class.forName(className);
                    if (configBlacklist.contains(simpleClassName)) continue;

                    var item = (IRelicItem) clazz.getDeclaredConstructor().newInstance();
                    RELICS.computeIfAbsent(item.getType(), a -> new ArrayList<>()).add(item);

                    LogHelper.debug(Strange.MOD_ID, getClass(), "Loaded relic: " + simpleClassName);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    LogHelper.warn(getClass(), "Relic `" + simpleClassName + "` failed to load: " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            LogHelper.info(Strange.MOD_ID, getClass(), "Failed to load classes from namespace: " + e.getMessage());
        }

        for (var lootTable : additionalLootTables) {
            var table = new ResourceLocation(lootTable);
            VALID_LOOT_TABLES.add(table);
        }
    }

    private void handlePlayerTick(Player player) {
        if (player.level.isClientSide || player.level.getGameTime() % 70 == 0) return;
        var serverPlayer = (ServerPlayer) player;

        if (CACHED_RELIC_ADVANCEMENT == null) {
            var advancements = serverPlayer.getLevel().getServer().getAdvancements();
            var id = new ResourceLocation(Strange.MOD_ID, "relics/" + TRIGGER_FIND_RELIC.getPath());
            CACHED_RELIC_ADVANCEMENT = advancements.getAdvancement(id);
        }

        if (CACHED_RELIC_ADVANCEMENT != null) {
            var progress = serverPlayer.getAdvancements().getOrStartProgress(CACHED_RELIC_ADVANCEMENT);

            if (!progress.isDone()) {
                var inventory = player.getInventory();
                var size = inventory.getContainerSize();
                for (int i = 0; i < size; i++) {
                    var inSlot = inventory.getItem(i);
                    if (inSlot.isEmpty()) continue;
                    if (NbtHelper.tagExists(inSlot, RELIC_TAG)) {
                        triggerFindRelic(serverPlayer);
                        break;
                    }
                }
            }
        }
    }

    public static void triggerFindRelic(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_FIND_RELIC);
    }

    public static void preserveHighestLevelEnchantment(Map<Enchantment, Integer> enchantments, ItemStack book, ItemStack output) {
        if (book.isEmpty() || output.isEmpty()) return;

        if (book.getItem() instanceof EnchantedBookItem) {
            Map<Enchantment, Integer> reset = new HashMap<>();
            var bookEnchants = EnchantmentHelper.getEnchantments(book);

            bookEnchants.forEach((e, l) -> {
                if (l > e.getMaxLevel()) {
                    reset.put(e, l);
                }
            });

            reset.forEach((e, l) -> {
                if (enchantments.containsKey(e)) {
                    enchantments.put(e, l);
                }
            });
        }

        EnchantmentHelper.setEnchantments(enchantments, output);
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            var builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.DIAMOND)
                    .setWeight(1)
                    .apply(() -> new RelicLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }

    public enum Type {
        TOOL,
        WEAPON,
        ARMOR,
        WEIRD
    }
}
