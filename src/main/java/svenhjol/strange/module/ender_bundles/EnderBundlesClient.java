package svenhjol.strange.module.ender_bundles;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.event.ItemTooltipImageCallback;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.ender_bundles.network.ClientReceiveUpdatedEnderInventory;
import svenhjol.strange.module.ender_bundles.network.ClientSendUpdateEnderInventory;

import java.util.Optional;

@SuppressWarnings("unused")
@ClientModule(module = EnderBundles.class)
public class EnderBundlesClient extends CharmModule {
    public static final ResourceLocation ENDER_BUNDLE_FILLED = new ResourceLocation("ender_bundle_filled");
    public static float CACHED_AMOUNT_FILLED = 0.0F;
    public static ItemStack CACHED_ITEM_STACK;

    public static ClientReceiveUpdatedEnderInventory CLIENT_RECEIVE_UPDATED_ENDER_INVENTORY;
    public static ClientSendUpdateEnderInventory CLIENT_SEND_UPDATE_ENDER_INVENTORY;

    @Override
    public void register() {
        // Set up a predicate so that the item icon can change from empty to filled.
        ModelPredicateProviderRegistryAccessor.callRegister(ENDER_BUNDLE_FILLED, (stack, level, entity, i)
            -> EnderBundleItem.getAmountFilled());

        ClientTickEvents.END_CLIENT_TICK.register(this::handleClientTick);
    }

    @Override
    public void runWhenEnabled() {
        ItemTooltipImageCallback.EVENT.register(this::handleItemTooltipImage);

        CACHED_ITEM_STACK = new ItemStack(EnderBundles.ENDER_BUNDLE);

        CLIENT_RECEIVE_UPDATED_ENDER_INVENTORY = new ClientReceiveUpdatedEnderInventory();
        CLIENT_SEND_UPDATE_ENDER_INVENTORY = new ClientSendUpdateEnderInventory();
    }

    private Optional<TooltipComponent> handleItemTooltipImage(ItemStack stack) {
        if (!Strange.LOADER.isEnabled(EnderBundles.class) || stack == null || !(stack.getItem() instanceof EnderBundleItem)) {
            return Optional.empty();
        }

        // Poll for enderinventory changes on the server at a faster rate when the player is hovering over an ender bundle.
        ClientHelper.getLevel().ifPresent(world -> {
            if (world.getGameTime() % 5 == 0) {
                CLIENT_SEND_UPDATE_ENDER_INVENTORY.send();
            }
        });

        Optional<Player> player = ClientHelper.getPlayer();
        if (player.isPresent()) {
            PlayerEnderChestContainer inventory = player.get().getEnderChestInventory();
            int size = inventory.getContainerSize();
            NonNullList<ItemStack> items = NonNullList.create();

            for (int i = 0; i < size; i++) {
                items.add(inventory.getItem(i));
            }

            return Optional.of(new EnderBundleTooltip(items));
        }

        return Optional.empty();
    }

    /**
     * Poll for enderinventory changes on the server.
     */
    private void handleClientTick(Minecraft client) {
        if (client == null || client.level == null || client.player == null) return;

        // do this sparingly
        if (client.level.getGameTime() % 60 == 0 && client.player.getInventory().contains(CACHED_ITEM_STACK)) {
            CLIENT_SEND_UPDATE_ENDER_INVENTORY.send();
        }
    }
}
