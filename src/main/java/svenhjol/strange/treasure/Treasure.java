package svenhjol.strange.treasure;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Module(mod = Strange.MOD_ID, description = "Rare tools and armor with higher enchantment levels and rare potions with longer duration and potency.")
public class Treasure extends CharmModule {
    public static final Identifier TREASURE_LOOT_ID = new Identifier(Strange.MOD_ID, "treasure_loot");
    public static LootFunctionType TREASURE_LOOT_FUNCTION;

    public static Map<ITreasureTool, Integer> TOOLS = new HashMap<>();
    public static Map<ITreasurePotion, Integer> POTIONS = new HashMap<>();

    private final List<Identifier> lootTables = new ArrayList<>();

    @Config(name = "Additional enchantment levels", description = "Number of levels above the maximum that tool enchantments can use.")
    public static int extraLevels = 3;

    @Config(name = "Add to ruin loot", description = "If true, treasure will be added to epic ruin loot.")
    public static boolean addToRuinLoot = false;

    @Override
    public void register() {
        TREASURE_LOOT_FUNCTION = RegistryHandler.lootFunctionType(TREASURE_LOOT_ID, new LootFunctionType(new TreasureLootFunction.Serializer()));
    }

    @Override
    public void init() {
        TreasureTools.init();
        TreasurePotions.init();

        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        if (!ModuleHandler.enabled("strange:rubble")) {
            Charm.LOG.info("Adding treasure to simple_dungeon loot");
            lootTables.add(LootTables.SIMPLE_DUNGEON_CHEST);
        } else {
            Charm.LOG.info("Adding treasure to rubble loot");
            lootTables.add(StrangeLoot.RUBBLE);
        }

        if (addToRuinLoot && ModuleHandler.enabled("strange:ruins")) {
            Charm.LOG.info("Adding treasure to epic ruin loot");
            lootTables.add(StrangeLoot.OVERWORLD_RUINS_EPIC);
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (lootTables.contains(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new TreasureLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }
}
