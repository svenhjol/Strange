package svenhjol.strange.module.ender_bundles;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.api.event.HoverSortItemsCallback;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.hover_sorting.HoverSorting;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.end_shrines.EndShrines;
import svenhjol.strange.module.ender_bundles.network.ServerReceiveUpdateEnderInventory;
import svenhjol.strange.module.ender_bundles.network.ServerSendUpdatedEnderInventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Ender bundles allow transfer of items to and from your ender chest.")
public class EnderBundles extends CharmModule {
    public static final String ENDER_ITEMS_TAG = "EnderItems";
    public static final ResourceLocation TRIGGER_USED_ENDER_BUNDLE = new ResourceLocation(Strange.MOD_ID, "used_ender_bundle");
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "ender_bundle_loot");

    public static ServerSendUpdatedEnderInventory SERVER_SEND_UPDATED_ENDER_INVENTORY;
    public static ServerReceiveUpdateEnderInventory SERVER_RECEIVE_UPDATE_ENDER_INVENTORY;

    public static LootItemFunctionType LOOT_FUNCTION;
    public static EnderBundleItem ENDER_BUNDLE;

    public static final List<ResourceLocation> VALID_LOOT_TABLES = new ArrayList<>();

    @Override
    public void register() {
        ENDER_BUNDLE = new EnderBundleItem(this);
        HoverSorting.SORTABLE.add(ENDER_BUNDLE);

        LOOT_FUNCTION = CommonRegistry.lootFunctionType(LOOT_ID, new LootItemFunctionType(new EnderBundleLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        HoverSortItemsCallback.EVENT.register(this::handleSortItems);

        SERVER_SEND_UPDATED_ENDER_INVENTORY = new ServerSendUpdatedEnderInventory();
        SERVER_RECEIVE_UPDATE_ENDER_INVENTORY = new ServerReceiveUpdateEnderInventory();

        var endShrinesEnabled = Strange.LOADER.isEnabled(EndShrines.class);
        VALID_LOOT_TABLES.add(endShrinesEnabled ? EndShrines.END_SHRINES_TREASURE : BuiltInLootTables.END_CITY_TREASURE);
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder fabricLootSupplierBuilder, LootTableLoadingCallback.LootTableSetter lootTableSetter) {
        if (VALID_LOOT_TABLES.contains(id)) {
            var builder =  FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.DIAMOND)
                    .setWeight(1)
                    .apply(() -> new EnderBundleLootFunction(new LootItemCondition[0])));

            fabricLootSupplierBuilder.withPool(builder);
        }
    }

    private void handleSortItems(ServerPlayer player, ItemStack stack, boolean direction) {
        if (stack.getItem() == EnderBundles.ENDER_BUNDLE) {
            PlayerEnderChestContainer enderChestInventory = player.getEnderChestInventory();
            List<ItemStack> contents = new LinkedList<>();

            for (int i = 0; i < enderChestInventory.getContainerSize(); i++) {
                ItemStack s = enderChestInventory.getItem(i);
                if (!s.isEmpty())
                    contents.add(s);
            }

            HoverSortItemsCallback.sortByScrollDirection(contents, direction);
            enderChestInventory.clearContent();
            contents.forEach(enderChestInventory::addItem);
        }
    }

    public static void triggerUsedEnderBundle(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, EnderBundles.TRIGGER_USED_ENDER_BUNDLE);
    }
}