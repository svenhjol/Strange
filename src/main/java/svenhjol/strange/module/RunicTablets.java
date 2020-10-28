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
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.item.RunicFragmentItem;
import svenhjol.strange.item.RunicTabletItem;
import svenhjol.strange.loot.RunicFragmentLootFunction;

@Module(mod = Strange.MOD_ID)
public class RunicTablets extends CharmModule {
    public static final Identifier RUNIC_FRAGMENT_LOOT_ID = new Identifier(Strange.MOD_ID, "runic_fragment_loot");
    public static LootFunctionType RUNIC_FRAGMENT_LOOT_FUNCTION;

    public static RunicTabletItem RUNIC_TABLET;
    public static RunicFragmentItem RUNIC_FRAGMENT;

    public static boolean addFragmentsToRuinLoot = true;
    public static boolean addFragmentsToStrongholdLoot = true;
    public static boolean addFragmentsToBastionLoot = true;

    @Override
    public void register() {
        RUNIC_TABLET = new RunicTabletItem(this);
        RUNIC_FRAGMENT = new RunicFragmentItem(this);

        RUNIC_FRAGMENT_LOOT_FUNCTION = RegistryHandler.lootFunctionType(RUNIC_FRAGMENT_LOOT_ID, new LootFunctionType(new RunicFragmentLootFunction.Serializer()));
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUIN_GENERAL);
        DecorationHelper.CHEST_LOOT_TABLES.add(StrangeLoot.RUIN_GENERAL);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (!ModuleHandler.enabled("strange:excavation"))
            return;

        if (addFragmentsToRuinLoot && id.equals(StrangeLoot.RUIN_GENERAL)
            || (addFragmentsToStrongholdLoot && id.equals(LootTables.STRONGHOLD_CORRIDOR_CHEST)
            || (addFragmentsToBastionLoot && id.equals(LootTables.BASTION_TREASURE_CHEST)))
        ) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new RunicFragmentLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }
}
