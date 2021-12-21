package svenhjol.strange.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;

public abstract class ClientReceive {
    private final ResourceLocation id;

    public ClientReceive(ResourceLocation id) {
        this.id = id;
        ClientPlayNetworking.registerGlobalReceiver(id, this::handleInternal);
    }

    private void handleInternal(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        LogHelper.debug(getClass(), "Received message `" + id + "` from server.");
        handle(client, buffer);
    }

    public abstract void handle(Minecraft client, FriendlyByteBuf buffer);
}
