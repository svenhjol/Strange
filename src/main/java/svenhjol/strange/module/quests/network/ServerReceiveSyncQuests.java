package svenhjol.strange.module.quests.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;
import svenhjol.strange.module.quests.Quests;

@Id("strange:request_quests")
public class ServerReceiveSyncQuests extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        server.execute(() -> Quests.SERVER_SEND_QUESTS.send(player));
    }
}
