package svenhjol.strange.module.bookmarks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.bookmarks.Bookmark;

import javax.annotation.Nullable;

/**
 * Client sends an empty request to create a new bookmark on the server.
 */
@Id("strange:create_bookmark")
public class ClientSendCreateBookmark extends ClientSender {
    @Nullable
    public Bookmark send(String name, ResourceLocation icon, boolean openImmediately) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return null;

        var uuid = mc.player.getUUID();
        var pos = mc.player.blockPosition();
        var dim = DimensionHelper.getDimension(mc.player.level);
        var bookmark = new Bookmark(uuid, name, pos, dim, icon);

        super.send(buf -> {
            buf.writeNbt(bookmark.save());
            buf.writeBoolean(openImmediately);
        });

        return bookmark;
    }
}
