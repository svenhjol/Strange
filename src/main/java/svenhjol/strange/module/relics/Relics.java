package svenhjol.strange.module.relics;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.rubble.Rubble;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class Relics extends CharmModule {
    public static final List<IRelicItem> RELICS = new ArrayList<>();
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "relics_loot");
    public static LootItemFunctionType LOOT_FUNCTION;

    private final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();

    @Config(name = "Additional levels", description = "Number of levels above the enchantment maximum level that can be applied to relics.\n" +
        "Enchantment levels are capped at level 10.")
    public static int extraLevels = 5;

    @Config(name = "Items", description = "List of relic items that will be loaded. Items at the top of the list are more common.")
    public static List<String> configItems = Arrays.asList(
        "angery_potato",
        "pickaxe_relic"
    );

    @Override
    public void register() {
        LOOT_FUNCTION = RegistryHelper.lootFunctionType(LOOT_ID, new LootItemFunctionType(new RelicsLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        VALID_LOOT_TABLES.add(Rubble.LOOT);

        String namespace = "svenhjol.strange.module.relics.item.";

        for (String itemName : configItems) {
            try {
                String className = StringHelper.snakeToUpperCamel(itemName);
                Class<?> clazz = Class.forName(namespace + className);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                IRelicItem item = (IRelicItem)constructor.newInstance();
                RELICS.add(item);
                LogHelper.debug(getClass(), "Loaded relic: " + itemName);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                LogHelper.warn(getClass(), "Relic `" + itemName + "` failed to load: " + e.getMessage());
            }
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new RelicsLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
