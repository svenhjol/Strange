package svenhjol.strange.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import svenhjol.charm.helper.LogHelper;

public abstract class ServerReceive {
    public abstract ResourceLocation id();

    public ServerReceive() {
        var id = id();
        ServerPlayNetworking.registerGlobalReceiver(id, this::handleInternal);
    }

    private void handleInternal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, FriendlyByteBuf buffer, PacketSender sender) {
        var id = id();
        LogHelper.debug(getClass(), "Received message `" + id + "` from server.");
        handle(server, buffer);
    }

    public abstract void handle(MinecraftServer server, FriendlyByteBuf buffer);
}
