package svenhjol.strange.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ClientSend {
    private final ResourceLocation id;

    public ClientSend(ResourceLocation id) {
        this.id = id;
    }

    public void send() {
        send(null);
    }

    public void send(@Nullable Consumer<FriendlyByteBuf> callback) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        if (callback != null) {
            callback.accept(buffer);
        }

        LogHelper.debug(getClass(), "Sending message `" + id + "` to server.");
        ClientPlayNetworking.send(id, buffer);
    }
}
