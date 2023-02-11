package svenhjol.strange.feature.waypoints;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.Charm;
import svenhjol.charm_core.annotation.Packet;
import svenhjol.charm_core.enums.PacketDirection;
import svenhjol.charm_core.iface.IPacketRequest;
import svenhjol.strange.Strange;

public class WaypointsNetwork {
    public static void register() {
        Strange.REGISTRY.packet(new WaypointInfo(), () -> WaypointsClient::handleWaypointInfo);
        Strange.REGISTRY.packet(new FlushWaypoint(), () -> WaypointsClient::handleFlushWaypoint);
    }

    @Packet(
        id = "charm:waypoint_info",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Send the title and color of the waypoint to the client."
    )
    public static class WaypointInfo implements IPacketRequest {
        private Component title;
        private DyeColor color;
        private boolean playSound;

        private WaypointInfo() {}

        @Override
        public void encode(FriendlyByteBuf buf) {
            buf.writeComponent(title);
            buf.writeEnum(color);
            buf.writeBoolean(playSound);
        }

        @Override
        public void decode(FriendlyByteBuf buf) {
            title = buf.readComponent();
            color = buf.readEnum(DyeColor.class);
            playSound = buf.readBoolean();
        }

        public Component getTitle() {
            return title;
        }

        public DyeColor getColor() {
            return color;
        }

        public boolean playSound() {
            return playSound;
        }

        public static void send(Player player, Component title, DyeColor color, boolean playSound) {
            var message = new WaypointInfo();
            message.title = title;
            message.color = color;
            message.playSound = playSound;
            Charm.NETWORK.send(message, player);
        }
    }

    @Packet(
        id = "charm:flush_waypoint",
        direction = PacketDirection.SERVER_TO_CLIENT,
        description = "Remove the last seen title and color on the client."
    )
    public static class FlushWaypoint implements IPacketRequest {
        private FlushWaypoint() {}

        public static void send(Player player) {
            Charm.NETWORK.send(new FlushWaypoint(), player);
        }
    }
}
