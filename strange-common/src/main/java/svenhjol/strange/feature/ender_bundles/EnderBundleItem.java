package svenhjol.strange.feature.ender_bundles;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm_core.base.CharmFeature;
import svenhjol.charm_core.base.CharmItem;
import svenhjol.charm_core.init.AdvancementHandler;

import java.util.Optional;

public class EnderBundleItem extends CharmItem {
    private static final int ITEM_BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public EnderBundleItem(CharmFeature module) {
        super(module, new Properties()
            .stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return EnderBundlesClient.CACHED_AMOUNT_FILLED > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, (int)(12 * EnderBundlesClient.CACHED_AMOUNT_FILLED));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bundle, Slot slot, ClickAction clickAction, Player player) {
        if (player.containerMenu instanceof ChestMenu && !(slot.container instanceof Inventory)) {
            // Don't allow inside ender chest inventory.
            return false;
        } else if (player.getAbilities().instabuild) {
            // Creative container is insane.
            return false;
        } else if (clickAction != ClickAction.SECONDARY) {
            // Don't do anything if right-clicked.
            return false;
        } else {
            var itemStack = slot.getItem();
            if (itemStack.isEmpty()) {
                playRemoveOneSound(player);
                var out = removeFirstStack(player);
                out.ifPresent(slot::safeInsert);
            } else if (itemStack.getItem().canFitInsideContainerItems()) {
                var out = addToBundle(player, itemStack);
                itemStack.setCount(out.getCount());
                playInsertSound(player);
            }
            player.containerMenu.broadcastChanges();
        }

        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (player.containerMenu instanceof ChestMenu && !(slot.container instanceof Inventory)) {
            // Don't allow inside ender chest inventory.
            return false;
        } else if (player.getAbilities().instabuild) {
            // Creative container is insane.
            return false;
        } else if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                removeFirstStack(player).ifPresent(slotAccess::set);
            } else {
                var out = addToBundle(player, otherStack);
                otherStack.setCount(out.getCount());
            }

            player.containerMenu.broadcastChanges();
            return true;
        }

        return false;
    }

    private static ItemStack addToBundle(Player player, ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem().canFitInsideContainerItems()) {
            var inventory = player.getEnderChestInventory();
            var out = inventory.addItem(stack);
            inventory.setChanged();

            if (!player.level.isClientSide()) {
                AdvancementHandler.trigger(EnderBundles.ADVANCEMENT, (ServerPlayer) player);
            }

            return out;
        }

        return stack;
    }

    private static Optional<ItemStack> removeFirstStack(Player player) {
        var inventory = player.getEnderChestInventory();
        var index = 0;
        var size = inventory.getContainerSize();

        for (int i = size - 1; i >= 0; i--) {
            if (!inventory.getItem(i).isEmpty()) {
                index = i;
            }
        }
        var stack = inventory.getItem(index);
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        var out = stack.copy();
        stack.setCount(0);
        inventory.setChanged();

        return Optional.of(out);
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
    }
}
