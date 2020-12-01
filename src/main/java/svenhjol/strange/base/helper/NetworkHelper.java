package svenhjol.strange.base.helper;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class NetworkHelper {
    public static void sendEmptyPacketToServer(Identifier id) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ClientSidePacketRegistry.INSTANCE.sendToServer(id, buffer);
    }

    public static void sendPacketToServer(Identifier id, Consumer<PacketByteBuf> callback) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ClientSidePacketRegistry.INSTANCE.sendToServer(id, buffer);
    }

    public static void sendEmptyPacketToClient(ServerPlayerEntity player, Identifier id) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buffer);
    }

    public static void sendPacketToServer(ServerPlayerEntity player, Identifier id, Consumer<PacketByteBuf> callback) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buffer);
    }
}
