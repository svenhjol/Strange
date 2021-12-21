package svenhjol.strange.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import svenhjol.charm.helper.LogHelper;

public abstract class ServerReceiver {
    private int warnings = 0;
    private ResourceLocation id;

    public ServerReceiver() {
        var id = id();
        ServerPlayNetworking.registerGlobalReceiver(id, this::handleInternal);
    }

    private ResourceLocation id() {
        if (id == null && getClass().isAnnotationPresent(Id.class)) {
            var annotation = getClass().getAnnotation(Id.class);
            id = new ResourceLocation(annotation.value());
        } else {
            throw new IllegalStateException("Missing ID");
        }

        return id;
    }

    private void handleInternal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl listener, FriendlyByteBuf buffer, PacketSender sender) {
        var id = id();
        LogHelper.debug(getClass(), "Received message `" + id + "` from server.");

        try {
            handle(server, player, buffer);
        } catch (Exception e) {
            if (warnings < 10) {
                LogHelper.warn(getClass(), "Exception when handling message from server: " + e.getMessage());
                warnings++;
            }
        }
    }

    public abstract void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer);
}
