package svenhjol.strange.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class ClientSender {
    private ResourceLocation id;

    public void send() {
        send(null);
    }

    public void send(@Nullable Consumer<FriendlyByteBuf> callback) {
        var id = id();
        var buffer = new FriendlyByteBuf(Unpooled.buffer());

        if (callback != null) {
            callback.accept(buffer);
        }

        LogHelper.debug(getClass(), "Sending message `" + id + "` to server.");
        ClientPlayNetworking.send(id, buffer);
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
}
