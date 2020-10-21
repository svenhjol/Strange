package svenhjol.strange.module;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.item.BlankTabletItem;
import svenhjol.strange.item.ClayTabletItem;
import svenhjol.strange.item.RunicTabletItem;
import svenhjol.strange.item.TabletItem;
import svenhjol.strange.scroll.loot.BlankTabletLootFunction;
import svenhjol.strange.scroll.loot.RunicTabletLootFunction;

@Module(mod = Strange.MOD_ID)
public class RunicTablets extends CharmModule {
    public static final Identifier RUNIC_TABLET_LOOT_ID = new Identifier(Strange.MOD_ID, "runic_tablet_loot");
    public static final Identifier BLANK_TABLET_LOOT_ID = new Identifier(Strange.MOD_ID, "blank_tablet_loot");
    public static LootFunctionType RUNIC_TABLET_LOOT_FUNCTION;
    public static LootFunctionType BLANK_TABLET_LOOT_FUNCTION;

    public static TabletItem CLAY_TABLET;
    public static TabletItem RUNIC_TABLET;
    public static TabletItem BLANK_TABLET;

    public static boolean addRunicTabletsToLoot = true;
    public static boolean addBlankTabletsToLoot = true;

    @Override
    public void register() {
        CLAY_TABLET = new ClayTabletItem(this, "clay_tablet");
        RUNIC_TABLET = new RunicTabletItem(this, "runic_tablet");
        BLANK_TABLET = new BlankTabletItem(this, "blank_tablet");

        RUNIC_TABLET_LOOT_FUNCTION = new LootFunctionType(new RunicTabletLootFunction.Serializer());
        BLANK_TABLET_LOOT_FUNCTION = new LootFunctionType(new BlankTabletLootFunction.Serializer());
        Registry.register(Registry.LOOT_FUNCTION_TYPE, RUNIC_TABLET_LOOT_ID, RUNIC_TABLET_LOOT_FUNCTION);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, BLANK_TABLET_LOOT_ID, BLANK_TABLET_LOOT_FUNCTION);
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.TABLET);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.TABLET);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (!ModuleHandler.enabled("strange:excavation"))
            return;

        // TODO: foundations loot

        if (addRunicTabletsToLoot || addBlankTabletsToLoot) {
            if (id.equals(StrangeLoot.TABLET)) {
                FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootTableRange.create(1))
                    .with(ItemEntry.builder(Items.AIR)
                        .weight(20)
                        .apply(() -> new RunicTabletLootFunction(new LootCondition[0])))
                    .with(ItemEntry.builder(Items.AIR)
                        .weight(10)
                        .apply(() -> new BlankTabletLootFunction(new LootCondition[0])));

                supplier.pool(builder);
            }
        }
    }
}
