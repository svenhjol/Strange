package svenhjol.strange.module.journals.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.journals.PageTracker.Page;

@Id("strange:journal_page")
public class ServerSendPage extends ServerSender {
    public void send(ServerPlayer player, Page page) {
        super.send(player, buf -> buf.writeEnum(page));
    }
}
