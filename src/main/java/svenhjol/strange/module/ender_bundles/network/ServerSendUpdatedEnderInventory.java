package svenhjol.strange.module.ender_bundles.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.ender_bundles.EnderBundles;

@Id("strange:updated_ender_inventory")
public class ServerSendUpdatedEnderInventory extends ServerSender {
    @Override
    protected boolean showDebugMessages() {
        return false;
    }

    @Override
    public void send(ServerPlayer player) {
        var tag = new CompoundTag();
        var inventory = player.getEnderChestInventory();
        tag.put(EnderBundles.ENDER_ITEMS_TAG, inventory.createTag());
        super.send(player, buf -> buf.writeNbt(tag));
    }
}
