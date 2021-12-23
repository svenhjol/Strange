package svenhjol.strange.module.journals.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.journals.Journals;

@Id("strange:journal")
public class ServerSendJournal extends ServerSender {
    @Override
    public void send(ServerPlayer player) {
        var journal = Journals.getJournal(player).orElse(null);
        if (journal == null) return;

        var tag = journal.save();
        super.send(player, buf -> buf.writeNbt(tag));
    }
}
