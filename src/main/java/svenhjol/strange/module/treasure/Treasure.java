package svenhjol.strange.module.treasure;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.module.rubble.Rubble;
import svenhjol.strange.module.ruins.Ruins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID, description = "Rare tools and armor with higher enchantment levels and rare potions with longer duration and potency.")
public class Treasure extends CharmModule {
    public static final ResourceLocation TREASURE_LOOT_ID = new ResourceLocation(Strange.MOD_ID, "treasure_loot");
    public static LootItemFunctionType TREASURE_LOOT_FUNCTION;

    public static Map<ITreasureTool, Integer> TOOLS = new HashMap<>();
    public static Map<ITreasurePotion, Integer> POTIONS = new HashMap<>();

    private final List<ResourceLocation> lootTables = new ArrayList<>();

    @Config(name = "Additional enchantment levels", description = "Number of levels above the maximum that tool enchantments can use.")
    public static int extraLevels = 3;

    @Config(name = "Add to ruin loot", description = "If true, treasure will be added to epic ruin loot.")
    public static boolean addToRuinLoot = false;

    @Override
    public void register() {
        TREASURE_LOOT_FUNCTION = RegistryHelper.lootFunctionType(TREASURE_LOOT_ID, new LootItemFunctionType(new TreasureLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        TreasureTools.init();
        TreasurePotions.init();

        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        if (!Strange.LOADER.isEnabled(Rubble.class)) {
            LogHelper.info(this.getClass(), "Adding treasure to simple_dungeon loot");
            lootTables.add(BuiltInLootTables.SIMPLE_DUNGEON);
        } else {
            LogHelper.info(this.getClass(), "Adding treasure to rubble loot");
            lootTables.add(StrangeLoot.RUBBLE);
        }

        if (addToRuinLoot && Strange.LOADER.isEnabled(Ruins.class)) {
            LogHelper.info(this.getClass(), "Adding treasure to epic ruin loot");
            lootTables.add(StrangeLoot.OVERWORLD_RUINS_EPIC);
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (lootTables.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new TreasureLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
