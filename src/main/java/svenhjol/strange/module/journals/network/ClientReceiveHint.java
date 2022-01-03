package svenhjol.strange.module.journals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.journals.JournalsClient;

@Id("strange:journal_hint")
public class ClientReceiveHint extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var hint = buffer.readBoolean();

        client.execute(() -> JournalsClient.showJournalHint = hint);
    }
}
