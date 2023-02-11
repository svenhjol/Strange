package svenhjol.strange.feature.ender_bundles;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import svenhjol.charm.Charm;
import svenhjol.charm.feature.hover_sorting.HoverSorting;
import svenhjol.charm.feature.inventory_tidying.InventoryTidyingHandler;
import svenhjol.charm_api.event.ItemHoverSortEvent;
import svenhjol.charm_api.iface.IChecksInventoryItem;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.helper.TextHelper;
import svenhjol.charm_core.init.CharmApi;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

@Feature(mod = Charm.MOD_ID, description = "A bundle that is linked to the player's Ender Chest.")
public class EnderBundles extends CharmFeature implements IChecksInventoryItem {
    public static final String ID = "ender_bundle";
    public static final String ENDER_ITEMS_TAG = "EnderItems";
    public static final Component CONTAINER_TITLE = TextHelper.translatable("container.strange.ender_bundle");
    public static final ResourceLocation ADVANCEMENT = Charm.makeId("used_ender_bundle");
    public static Supplier<EnderBundleItem> ITEM;

    @Override
    public void register() {
        ITEM = Strange.REGISTRY.item(ID, () -> new EnderBundleItem(this));

        EnderBundlesNetwork.register();
        CharmApi.registerProvider(this);
    }

    @Override
    public void runWhenEnabled() {
        ItemHoverSortEvent.INSTANCE.handle(this::handleItemHoverSort);
        HoverSorting.addSortable(ITEM.get());
    }

    private void handleItemHoverSort(ServerPlayer player, ItemStack stack, ItemHoverSortEvent.SortDirection direction) {
        if (stack.is(ITEM.get())) {
            var items = new ArrayList<>(getEnderInventory(player));
            InventoryTidyingHandler.mergeStacks(items);
            ItemHoverSortEvent.sortByScrollDirection(items, direction);

            var inventory = player.getEnderChestInventory();
            var size = inventory.getContainerSize();

            for (int i = 0; i < size; i++) {
                if (i < items.size()) {
                    inventory.setItem(i, items.get(i));
                } else {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Called when the client requests an updated Ender inventory.
     * @param message Network packet from the client.
     * @param player Player who requested the inventory.
     */
    public static void handleRequestedInventory(EnderBundlesNetwork.RequestInventory message, Player player) {
        if (player != null) {
            var items = player.getEnderChestInventory().createTag();
            EnderBundlesNetwork.SendInventory.send(items, (ServerPlayer)player);
        }
    }

    public static void handleOpenedInventory(EnderBundlesNetwork.OpenInventory message, Player player) {
        if (!hasEnderBundle(player) || player.level.isClientSide()) return;

        var serverPlayer = (ServerPlayer)player;
        var enderInventory = serverPlayer.getEnderChestInventory();

        serverPlayer.closeContainer();
        serverPlayer.openMenu(new SimpleMenuProvider(
            (id, inventory, p) -> ChestMenu.threeRows(id, inventory, enderInventory), CONTAINER_TITLE));
    }

    public static boolean hasEnderBundle(Player player) {
        return CharmApi.getProviderData(IChecksInventoryItem.class, provider -> provider.getInventoryItemChecks().stream())
            .stream().anyMatch(check -> check.test(player, ITEM.get()));
    }

    public static NonNullList<ItemStack> getEnderInventory(Player player) {
        var inventory = player.getEnderChestInventory();
        var size = inventory.getContainerSize();
        var items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < size; i++) {
            items.set(i, inventory.getItem(i));
        }
        return items;
    }

    @Override
    public List<BiPredicate<Player, ItemLike>> getInventoryItemChecks() {
        return List.of((player, item) -> {
            List<ItemStack> items = new ArrayList<>();
            items.addAll(player.getInventory().items);
            items.addAll(player.getInventory().offhand);
            return items.stream().anyMatch(stack -> stack.is((Item)item));
        });
    }
}
