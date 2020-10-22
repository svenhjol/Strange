package svenhjol.strange.module;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.iface.ILegendaryPotion;
import svenhjol.strange.iface.ILegendaryTool;
import svenhjol.strange.legendary.items.AmbitiousCrossbow;
import svenhjol.strange.legendary.items.EldritchBow;
import svenhjol.strange.legendary.potions.Luck;
import svenhjol.strange.loot.LegendaryItemLootFunction;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID)
public class LegendaryItems extends CharmModule {
    public static final Identifier LEGENDARY_ITEMS_LOOT_ID = new Identifier(Strange.MOD_ID, "legendary_items_loot");
    public static LootFunctionType LEGENDARY_ITEMS_LOOT_FUNCTION;

    public static Map<Integer, ILegendaryTool> LEGENDARY_TOOLS = new HashMap<>();
    public static Map<Integer, ILegendaryPotion> LEGENDARY_POTIONS = new HashMap<>();

    private Identifier lootTable;

    @Override
    public void register() {
        // TODO: config to enable/disable each item/group
        LEGENDARY_TOOLS.put(1, new AmbitiousCrossbow());
        LEGENDARY_TOOLS.put(1, new EldritchBow());
        LEGENDARY_POTIONS.put(1, new Luck());

        LEGENDARY_ITEMS_LOOT_FUNCTION = new LootFunctionType(new LegendaryItemLootFunction.Serializer());
        Registry.register(Registry.LOOT_FUNCTION_TYPE, LEGENDARY_ITEMS_LOOT_ID, LEGENDARY_ITEMS_LOOT_FUNCTION);
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        if (!ModuleHandler.enabled("strange:excavation")) {
            if (!ModuleHandler.enabled("strange:runic_tablets")) {
                Charm.LOG.info("Adding legendary items to simple_dungeon loot");
                lootTable = LootTables.SIMPLE_DUNGEON_CHEST;
            } else {
                Charm.LOG.info("Adding legendary items to tablet loot");
                lootTable = StrangeLoot.TABLET;
            }
        } else {
            Charm.LOG.info("Adding legendary items to ancient_rubble loot");
            lootTable = StrangeLoot.ANCIENT_RUBBLE;
        }
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (id.equals(lootTable)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new LegendaryItemLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }
}
