package svenhjol.strange.module;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.item.TabletItem;

@Module(mod = Strange.MOD_ID)
public class RunicTablets extends CharmModule {
    public static TabletItem CLAY_TABLET;
    public static TabletItem RUNIC_TABLET;

    public static boolean addRunicTabletsToLoot = true;

    @Override
    public void register() {
        CLAY_TABLET = new TabletItem(this, "clay_tablet");
        RUNIC_TABLET = new TabletItem(this, "runic_tablet");
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (!ModuleHandler.enabled("strange:excavation") || !addRunicTabletsToLoot)
            return;

        if (id.equals(StrangeLoot.ANCIENT_RUBBLE)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(RUNIC_TABLET)
                    .weight(20));

            supplier.pool(builder);
        }
    }
}
