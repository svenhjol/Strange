package svenhjol.strange.module.journals.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker.Page;

@Id("strange:journal_page")
public class ClientReceivePage extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var page = buffer.readEnum(Page.class);

        client.execute(() -> {
            LogHelper.debug(getClass(), "Wants to open journal page `" + page + "`.");
            client.setScreen(JournalsClient.tracker.getScreen(page));
        });
    }
}
