package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

@Id("strange:create_death_bookmark")
public class ServerSendCreateDeathBookmark extends ServerSender {
    public void send(ServerPlayer player, Component deathMessage) {
        super.send(player, buf -> buf.writeComponent(deathMessage));
    }
}
