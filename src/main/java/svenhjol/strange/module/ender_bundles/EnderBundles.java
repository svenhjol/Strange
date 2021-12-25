package svenhjol.strange.module.ender_bundles;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.hover_sorting.HoverSorting;
import svenhjol.charm.api.event.HoverSortItemsCallback;
import svenhjol.strange.Strange;
import svenhjol.strange.module.ender_bundles.network.ServerReceiveUpdateEnderInventory;
import svenhjol.strange.module.ender_bundles.network.ServerSendUpdatedEnderInventory;

import java.util.LinkedList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Ender bundles allow transfer of items to and from your ender chest.")
public class EnderBundles extends CharmModule {
    public static final String ENDER_ITEMS_TAG = "EnderItems";
    public static final ResourceLocation TRIGGER_USED_ENDER_BUNDLE = new ResourceLocation(Strange.MOD_ID, "used_ender_bundle");

    public static ServerSendUpdatedEnderInventory SERVER_SEND_UPDATED_ENDER_INVENTORY;
    public static ServerReceiveUpdateEnderInventory SERVER_RECEIVE_UPDATE_ENDER_INVENTORY;

    public static EnderBundleItem ENDER_BUNDLE;

    @Override
    public void register() {
        ENDER_BUNDLE = new EnderBundleItem(this);
        HoverSorting.SORTABLE.add(ENDER_BUNDLE);
    }

    @Override
    public void runWhenEnabled() {
        HoverSortItemsCallback.EVENT.register(this::handleSortItems);

        SERVER_SEND_UPDATED_ENDER_INVENTORY = new ServerSendUpdatedEnderInventory();
        SERVER_RECEIVE_UPDATE_ENDER_INVENTORY = new ServerReceiveUpdateEnderInventory();
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