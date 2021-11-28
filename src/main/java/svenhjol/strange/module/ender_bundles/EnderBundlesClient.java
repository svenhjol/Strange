package svenhjol.strange.module.ender_bundles;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.event.ItemTooltipImageCallback;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;

import java.util.Optional;

@SuppressWarnings("unused")
@ClientModule(module = EnderBundles.class)
public class EnderBundlesClient extends CharmModule {
    public static final ResourceLocation ENDER_BUNDLE_FILLED = new ResourceLocation("ender_bundle_filled");
    public static float CACHED_AMOUNT_FILLED = 0.0F;
    private static boolean isEnabled = false;

    @Override
    public void register() {
        // set up item predicate so the icon changes when full
        ModelPredicateProviderRegistryAccessor.callRegister(ENDER_BUNDLE_FILLED, (stack, level, entity, i)
            -> EnderBundleItem.getAmountFilled());

        // register callbacks
        ClientPlayNetworking.registerGlobalReceiver(EnderBundles.MSG_CLIENT_UPDATE_ENDER_INVENTORY, this::handleClientUpdateEnderInventory);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleClientTick);
    }

    @Override
    public void runWhenEnabled() {
        ItemTooltipImageCallback.EVENT.register(this::handleItemTooltipImage);
        isEnabled = true;
    }

    private Optional<TooltipComponent> handleItemTooltipImage(ItemStack stack) {
        if (!isEnabled || stack == null || !(stack.getItem() instanceof EnderBundleItem)) {
            return Optional.empty();
        }

        // Poll for enderinventory changes on the server at a faster rate when the player is hovering over an ender bundle.
        ClientHelper.getLevel().ifPresent(world -> {
            if (world.getGameTime() % 5 == 0) {
                NetworkHelper.sendEmptyPacketToServer(EnderBundles.MSG_SERVER_UPDATE_ENDER_INVENTORY);
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
     * Handle message sent from the server containing updated ender inventory.
     */
    private void handleClientUpdateEnderInventory(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        client.execute(() ->
            ClientHelper.getPlayer().ifPresent(player -> {
                if (tag != null && tag.contains(EnderBundles.TAG_ENDER_ITEMS, 9)) {
                    ListTag enderItems = tag.getList(EnderBundles.TAG_ENDER_ITEMS, 10);
                    PlayerEnderChestContainer inventory = player.getEnderChestInventory();
                    inventory.fromTag(enderItems);

                    CACHED_AMOUNT_FILLED = (float)enderItems.size() / inventory.getContainerSize();
                }
            }));
    }

    /**
     * Poll for enderinventory changes on the server.
     */
    private void handleClientTick(Minecraft client) {
        if (client == null || client.level == null || client.player == null)
            return;

        // do this sparingly
        if (client.level.getGameTime() % 60 == 0) {
            NetworkHelper.sendEmptyPacketToServer(EnderBundles.MSG_SERVER_UPDATE_ENDER_INVENTORY);
        }
    }
}
