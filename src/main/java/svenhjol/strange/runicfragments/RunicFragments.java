package svenhjol.strange.runicfragments;

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
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;

import java.util.ArrayList;
import java.util.List;

@Module(mod = Strange.MOD_ID, description = "Runic fragments are loot items that provide co-ordinates to rare structures.")
public class RunicFragments extends CharmModule {
    public static final Identifier LOOT_ID = new Identifier(Strange.MOD_ID, "runic_fragment_loot");
    public static LootFunctionType LOOT_FUNCTION;
    public static RunicFragmentItem RUNIC_FRAGMENT;
    public static List<Identifier> DESTINATIONS = new ArrayList<>();

    @Config(name = "Loot chance", description = "Chance (out of 1.0) of a runic fragment appearing in rubble (or stronghold loot if excavation is disabled).")
    public static double lootChance = 0.5F;

    @Override
    public void register() {
        RunicFragments.RUNIC_FRAGMENT = new RunicFragmentItem(this);
        RunicFragments.LOOT_FUNCTION = RegistryHandler.lootFunctionType(RunicFragments.LOOT_ID, new LootFunctionType(new RunicFragmentLootFunction.Serializer()));
    }

    @Override
    public void init() {
        // listen for loot events so that we can add the fragment to loot tables on demand
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        boolean excavationEnabled = ModuleHandler.enabled("strange:excavation");

        if ((excavationEnabled && id.equals(StrangeLoot.RUBBLE)) || (!excavationEnabled && id.equals(LootTables.STRONGHOLD_CROSSING_CHEST))) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootTableRange.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new RunicFragmentLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }
}
