package svenhjol.strange.legendaryitems;

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
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.legendaryitems.items.*;
import svenhjol.strange.legendaryitems.potions.LegendaryPotion;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID)
public class LegendaryItems extends CharmModule {
    public static final Identifier LEGENDARY_ITEMS_LOOT_ID = new Identifier(Strange.MOD_ID, "legendary_items_loot");
    public static LootFunctionType LEGENDARY_ITEMS_LOOT_FUNCTION;

    public static Map<ILegendaryEnchanted, Integer> LEGENDARY_ENCHANTED = new HashMap<>();
    public static Map<ILegendaryPotion, Integer> LEGENDARY_POTIONS = new HashMap<>();

    private Identifier lootTable;

    @Config(name = "Additional enchantment levels", description = "Number of levels above the maximum that legendary enchantments can use.")
    public static int extraLevels = 3;

    @Override
    public void register() {
        // TODO: config to enable/disable each item/group
        LEGENDARY_ENCHANTED.put(new LegendaryAxe(), 4);
        LEGENDARY_ENCHANTED.put(new LegendaryBoots(), 3);
        LEGENDARY_ENCHANTED.put(new LegendaryBow(), 4);
        LEGENDARY_ENCHANTED.put(new LegendaryChestplate(), 3);
        LEGENDARY_ENCHANTED.put(new LegendaryCrossbow(), 4);
        LEGENDARY_ENCHANTED.put(new LegendaryFishingRod(), 2);
        LEGENDARY_ENCHANTED.put(new LegendaryHelmet(), 3);
        LEGENDARY_ENCHANTED.put(new LegendaryLeggings(), 3);
        LEGENDARY_ENCHANTED.put(new LegendaryPickaxe(), 4);
        LEGENDARY_ENCHANTED.put(new LegendaryShield(), 5);
        LEGENDARY_ENCHANTED.put(new LegendaryShovel(), 4);
        LEGENDARY_ENCHANTED.put(new LegendarySword(), 4);
        LEGENDARY_ENCHANTED.put(new LegendaryTrident(), 2);
        LEGENDARY_ENCHANTED.put(new AngeryPotato(), 1);
        LEGENDARY_ENCHANTED.put(new AmbitiousCrossbow(), 1);
        LEGENDARY_ENCHANTED.put(new EldritchBow(), 1);
        LEGENDARY_ENCHANTED.put(new NeedleSword(), 1);
        LEGENDARY_ENCHANTED.put(new WyvernAxe(), 1);

        LEGENDARY_POTIONS.put(new LegendaryPotion(), 1);

        LEGENDARY_ITEMS_LOOT_FUNCTION = RegistryHandler.lootFunctionType(LEGENDARY_ITEMS_LOOT_ID, new LootFunctionType(new LegendaryItemLootFunction.Serializer()));
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        if (!ModuleHandler.enabled("strange:excavation")) {
            if (!ModuleHandler.enabled("strange:ruins")) {
                Charm.LOG.info("Adding legendary items to simple_dungeon loot");
                lootTable = LootTables.SIMPLE_DUNGEON_CHEST;
            } else {
                Charm.LOG.info("Adding legendary items to ruin loot");
                lootTable = StrangeLoot.RUIN_RARE;
            }
        } else {
            Charm.LOG.info("Adding legendary items to ancient_rubble loot");
            lootTable = StrangeLoot.RUBBLE;
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
