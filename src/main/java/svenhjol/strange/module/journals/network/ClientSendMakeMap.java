package svenhjol.strange.module.journals.network;

import svenhjol.charm.network.ClientSender;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.bookmarks.Bookmark;

@Id("strange:make_map")
public class ClientSendMakeMap extends ClientSender {
    public void send(Bookmark bookmark) {
        super.send(buf -> buf.writeNbt(bookmark.save()));
    }
}
