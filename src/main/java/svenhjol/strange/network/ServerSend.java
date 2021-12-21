package svenhjol.strange.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.helper.LogHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ServerSend {
    public abstract ResourceLocation id();

    public void send(ServerPlayer player) {
        send(player, null);
    }

    public void send(ServerPlayer player, @Nullable Consumer<FriendlyByteBuf> callback) {
        var id = id();
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        if (callback != null) {
            callback.accept(buffer);
        }

        LogHelper.debug(getClass(), "Sending message `" + id + "` to " + player.getUUID());
        ServerPlayNetworking.send(player, id, buffer);
    }

    public void sendToAll(MinecraftServer server) {
        sendToAll(server, null);
    }

    public void sendToAll(MinecraftServer server, @Nullable Consumer<FriendlyByteBuf> callback) {
        var playerList = server.getPlayerList();
        playerList.getPlayers().forEach(player -> send(player, callback));
    }
}
