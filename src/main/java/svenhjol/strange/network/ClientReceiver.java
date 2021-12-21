package svenhjol.strange.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;

public abstract class ClientReceiver {
    private ResourceLocation id;
    private int warnings = 0;

    public ClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(id(), this::handleInternal);
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

    private void handleInternal(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        LogHelper.debug(getClass(), "Received message `" + id + "` from server.");

        try {
            handle(client, buffer);
        } catch (Exception e) {
            if (warnings < 10) {
                LogHelper.warn(getClass(), "Exception when handling message from client: " + e.getMessage());
                warnings++;
            }
        }
    }

    public abstract void handle(Minecraft client, FriendlyByteBuf buffer);
}
