package svenhjol.strange.module.journals.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.journals.Journals;

@Id("strange:open_journal")
public class ServerReceiveOpenJournal extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        server.execute(() -> {
            Journals.getJournal(player).ifPresent(
                journal -> journal.setOpenedJournal(true));

            Journals.triggerOpenJournal(player);
        });
    }
}
