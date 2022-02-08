package svenhjol.strange.module.journals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.discoveries.Discovery;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.PageTracker;

@Id("strange:unignore_discovery")
public class ServerReceiveUnignoreDiscovery extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        var tag = getCompoundTag(buffer).orElseThrow();

        server.execute(() -> {
            var journal = Journals.getJournal(player).orElse(null);
            if (journal == null) return;

            var discovery = Discovery.load(tag);
            journal.unignoreDiscovery(discovery);

            Journals.SERVER_SEND_JOURNAL.send(player);
            Journals.SERVER_SEND_PAGE.send(player, PageTracker.Page.DISCOVERIES);
        });
    }
}
