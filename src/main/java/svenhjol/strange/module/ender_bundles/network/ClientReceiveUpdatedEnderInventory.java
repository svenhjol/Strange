package svenhjol.strange.module.ender_bundles.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.ender_bundles.EnderBundles;
import svenhjol.strange.module.ender_bundles.EnderBundlesClient;

@Id("strange:updated_ender_inventory")
public class ClientReceiveUpdatedEnderInventory extends ClientReceiver {
    @Override
    protected boolean showDebugMessages() {
        return false;
    }

    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();
        if (!tag.contains(EnderBundles.ENDER_ITEMS_TAG, 9)) return;

        client.execute(() -> ClientHelper.getPlayer().ifPresent(player -> {
            ListTag items = tag.getList(EnderBundles.ENDER_ITEMS_TAG, 10);
            PlayerEnderChestContainer inventory = player.getEnderChestInventory();
            inventory.fromTag(items);
            var size = inventory.getContainerSize();

            EnderBundlesClient.CACHED_AMOUNT_FILLED = (float)items.size() / size;
        }));
    }
}
