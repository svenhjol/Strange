package svenhjol.strange.module.bookmarks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.network.Id;
import svenhjol.strange.network.ServerReceiver;

@Id("strange:add_bookmark")
public class ServerReceiveBookmark extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {

    }
}
