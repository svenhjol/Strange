package svenhjol.strange.helper;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class NetworkHelper {
    public static void sendEmptyPacketToServer(Identifier id) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(id, buffer);
    }

    public static void sendPacketToServer(Identifier id, Consumer<PacketByteBuf> callback) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ClientPlayNetworking.send(id, buffer);
    }

    public static void sendEmptyPacketToClient(ServerPlayerEntity player, Identifier id) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, id, buffer);
    }

    public static void sendPacketToServer(ServerPlayerEntity player, Identifier id, Consumer<PacketByteBuf> callback) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ServerPlayNetworking.send(player, id, buffer);
    }
}
