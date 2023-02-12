package svenhjol.strange.feature.ender_bundles;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.Charm;
import svenhjol.charm.CharmClient;
import svenhjol.charm_core.annotation.Packet;
import svenhjol.charm_core.enums.PacketDirection;
import svenhjol.charm_core.iface.IPacketRequest;

import javax.annotation.Nullable;

public class EnderBundlesNetwork {
    public static void register() {
        Charm.REGISTRY.packet(new RequestInventory(), () -> EnderBundles::handleRequestedInventory);
        Charm.REGISTRY.packet(new OpenInventory(), () -> EnderBundles::handleOpenedInventory);
        Charm.REGISTRY.packet(new SendInventory(), () -> EnderBundlesClient::handleReceivedInventory);
    }

    @Packet(
        id = "strange:request_ender_inventory",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to instruct the server to send updated Ender inventory."
    )
    public static class RequestInventory implements IPacketRequest {
        private RequestInventory() {}

        public static void send() {
            CharmClient.NETWORK.send(new RequestInventory());
        }
    }

    @Packet(
        id = "strange:open_ender_inventory",
        direction = PacketDirection.CLIENT_TO_SERVER,
        description = "An empty packet sent from the client to instruct the server to open the Ender inventory."
    )
    public static class OpenInventory implements IPacketRequest {
        private OpenInventory() {}

        public static void send() {
            CharmClient.NETWORK.send(new OpenInventory());
        }
    }

    @Packet(
        id = "strange:send_ender_inventory",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "The player's Ender inventory encoded as a list of items to be updated on the client."
    )
    public static class SendInventory implements IPacketRequest {
        @Nullable
        private ListTag items;

        private SendInventory() {}

        public static void send(ListTag items, ServerPlayer player) {
            var message = new SendInventory();
            message.items = items;
            Charm.NETWORK.send(message, player);
        }

        @Override
        public void encode(FriendlyByteBuf buf) {
            if (items != null) {
                var tag = new CompoundTag();
                tag.put(EnderBundles.ENDER_ITEMS_TAG, items);
                buf.writeNbt(tag);
            }
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            var tag = buf.readNbt();
            if (tag != null) {
                items = tag.getList(EnderBundles.ENDER_ITEMS_TAG, 10);
            }
        }

        @Nullable
        public ListTag getItems() {
            return items;
        }
    }
}
