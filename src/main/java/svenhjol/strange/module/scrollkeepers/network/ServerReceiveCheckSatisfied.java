package svenhjol.strange.module.scrollkeepers.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.scrollkeepers.Scrollkeepers;

/**
 * Server receives request from client to check whether the player has any satisfied quests.
 */
@Id("strange:check_scrollkeeper_satisfied")
public class ServerReceiveCheckSatisfied extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        server.execute(() -> Scrollkeepers.SERVER_SEND_SATISFIED.send(player));
    }

    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}
