package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.bookmarks.DefaultIcon;

@Id("strange:create_death_bookmark")
public class ClientReceiveCreateDeathBookmark extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var deathMessage = buffer.readComponent();

        client.execute(() -> {
            var name = deathMessage.getString();
            BookmarksClient.SEND_CREATE_BOOKMARK.send(name, DefaultIcon.DEATH.getId(), false);
        });
    }
}
