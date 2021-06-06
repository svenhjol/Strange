package svenhjol.strange.helper;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;

// TODO: move to Charm
public class NetworkHelper {
    public static void sendEmptyPacketToServer(ResourceLocation id) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(id, buffer);
    }

    public static void sendPacketToServer(ResourceLocation id, Consumer<FriendlyByteBuf> callback) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ClientPlayNetworking.send(id, buffer);
    }

    public static void sendEmptyPacketToClient(ServerPlayer player, ResourceLocation id) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, id, buffer);
    }

    public static void sendPacketToServer(ServerPlayer player, ResourceLocation id, Consumer<FriendlyByteBuf> callback) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        callback.accept(buffer);
        ServerPlayNetworking.send(player, id, buffer);
    }
}
