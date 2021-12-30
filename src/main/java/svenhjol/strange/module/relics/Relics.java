package svenhjol.strange.module.relics;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
import svenhjol.charm.helper.ClassHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
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

@CommonModule(mod = Strange.MOD_ID)
public class Relics extends CharmModule {
    public static final String ITEM_NAMESPACE = "svenhjol.strange.module.relics.item";
    public static final List<IRelicItem> RELICS = new ArrayList<>();
    public static final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "relics_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    @Config(name = "Additional levels", description = "Number of levels above the enchantment maximum level that can be applied to relics.\n" +
        "Enchantment levels are capped at level 10.")
    public static int extraLevels = 5;

    @Config(name = "Additional loot tables", description = "List of additional loot tables that relics will be added to.")
    public static List<String> additionalLootTables = List.of();


    @Config(name = "Blacklist", description = "List of relic items that will not be loaded. See wiki for details.")
    public static List<String> configBlacklist = new ArrayList<>();

    @Override
    public void register() {
        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new RelicsLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        if (Strange.LOADER.isEnabled(Vaults.class)) {

            // If vaults is enabled, add relics to the large room.
            VALID_LOOT_TABLES.add(VaultsLoot.VAULTS_LARGE_ROOM);

        } else if (Strange.LOADER.isEnabled(StoneRuins.class)) {

            // If vaults not enabled and stone ruins are enabled, add relics to the ruin rooms.
            VALID_LOOT_TABLES.add(StoneRuinsLoot.STONE_RUINS_ROOM);

        } else {

            // Default to adding relics to woodland mansions.
            VALID_LOOT_TABLES.add(BuiltInLootTables.WOODLAND_MANSION);

        }

        try {
            List<String> classes = ClassHelper.getClassesInPackage(ITEM_NAMESPACE);
            for (String className : classes) {
                String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (configBlacklist.contains(simpleClassName)) continue;

                    IRelicItem item = (IRelicItem) clazz.getDeclaredConstructor().newInstance();
                    RELICS.add(item);
                    LogHelper.debug(Strange.MOD_ID, getClass(), "Loaded relic: " + simpleClassName);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    LogHelper.warn(getClass(), "Relic `" + simpleClassName + "` failed to load: " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            LogHelper.info(Strange.MOD_ID, getClass(), "Failed to load classes from namespace: " + e.getMessage());
        }

        for (String lootTable : additionalLootTables) {
            VALID_LOOT_TABLES.add(new ResourceLocation(lootTable));
        }
    }

    public static void preserveHighestLevelEnchantment(Map<Enchantment, Integer> enchantments, ItemStack book, ItemStack output) {
        if (book.isEmpty() || output.isEmpty()) return;

        if (book.getItem() instanceof EnchantedBookItem) {
            Map<Enchantment, Integer> reset = new HashMap<>();
            Map<Enchantment, Integer> bookEnchants = EnchantmentHelper.getEnchantments(book);

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
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.DIAMOND_SWORD)
                    .setWeight(1)
                    .apply(() -> new RelicsLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
