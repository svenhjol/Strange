package svenhjol.strange.module.bookmarks;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.BookmarkMessages;
import svenhjol.strange.module.bookmarks.branch.BookmarkBranch;

@ClientModule(module = Bookmarks.class)
public class BookmarksClient extends CharmModule {
    public static BookmarkBranch bookmarks;

    @Override
    public void runWhenEnabled() {
        ClientEntityEvents.ENTITY_LOAD.register(this::handlePlayerJoin);
        ClientPlayNetworking.registerGlobalReceiver(BookmarkMessages.CLIENT_SYNC_BOOKMARKS, this::handleSyncBookmarks);
    }

    private void handlePlayerJoin(Entity entity, ClientLevel level) {
        if (!(entity instanceof LocalPlayer)) return;

        // Ask the server for all bookmarks to be sent.
        NetworkHelper.sendEmptyPacketToServer(BookmarkMessages.SERVER_SYNC_BOOKMARKS);
    }

    private void handleSyncBookmarks(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null || tag.isEmpty()) return;
        client.execute(() -> {
            bookmarks = BookmarkBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + bookmarks.size() + " bookmarks from server.");
        });
    }
}
