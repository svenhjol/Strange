package svenhjol.strange.module.journals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.journals.Journals.Page;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;

import java.util.Optional;
import java.util.function.Consumer;

@ClientModule(module = Journals.class)
public class JournalsClient extends CharmModule {
    private static final int MAX_PHOTO_TICKS = 30;

    private KeyMapping keyBinding;
    private static JournalData journal;

    public static JournalBookmark bookmarkBeingPhotographed;
    public static int photoTicks = 0;

    @Override
    public void runWhenEnabled() {
        if (Journals.enableKeybind) {
            keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.charm.openJournal",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "key.categories.inventory"
            ));

            ClientTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
        }

        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_OPEN_JOURNAL, this::handleOpenJournal);
        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_SYNC_JOURNAL, this::handleSyncJournal);
        ClientPlayNetworking.registerGlobalReceiver(Journals.MSG_CLIENT_OPEN_BOOKMARK, this::handleOpenBookmark);
    }

    /**
     * Always use this method to reference the current player's journal data on the client.
     * If you need to synchronise it, call sendSyncJournal() or sendOpenJournal() to sync and open
     */
    public static Optional<JournalData> getJournalData() {
        return Optional.ofNullable(journal);
    }

    private void handleKeyPressed() {
        sendOpenJournal(Page.HOME);
    }

    private void handleWorldTick(Level level) {
        if (keyBinding == null || level == null) return;

        while (keyBinding.consumeClick()) {
            handleKeyPressed();
        }

        ClientHelper.getClient().ifPresent(this::handleTakingPhoto);
    }

    private void handleSyncJournal(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        updateJournal(buffer.readNbt());
    }

    private void handleOpenJournal(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        updateJournal(buffer.readNbt());
        Page page = buffer.readEnum(Page.class);
        JournalScreen screen = JournalViewer.getScreen(page);
        processPacketFromServer(client, mc -> mc.setScreen(screen));
    }

    private void handleOpenBookmark(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag bookmarkNbt = buffer.readNbt();
        if (bookmarkNbt == null)
            return;

        JournalBookmark newBookmark = JournalBookmark.fromTag(bookmarkNbt);
        processPacketFromServer(client, mc -> mc.setScreen(new JournalBookmarkScreen(newBookmark)));
    }

    public static void sendSyncJournal() {
        NetworkHelper.sendEmptyPacketToServer(Journals.MSG_SERVER_SYNC_JOURNAL);
    }

    public static void sendOpenJournal(Page page) {
        NetworkHelper.sendPacketToServer(Journals.MSG_SERVER_OPEN_JOURNAL, data -> data.writeEnum(page));
    }

    public static void sendAddBookmark() {
        NetworkHelper.sendEmptyPacketToServer(Journals.MSG_SERVER_ADD_BOOKMARK);
    }

    public static void sendUpdateBookmark(JournalBookmark bookmark) {
        NetworkHelper.sendPacketToServer(Journals.MSG_SERVER_UPDATE_BOOKMARK, data -> data.writeNbt(bookmark.toTag()));
    }

    public static void sendDeleteBookmark(JournalBookmark bookmark) {
        // delete on client then sync with server via a packet
        journal.getBookmarks().remove(bookmark);
        NetworkHelper.sendPacketToServer(Journals.MSG_SERVER_DELETE_BOOKMARK, data -> data.writeNbt(bookmark.toTag()));
    }

    public static void sendMakeMap(JournalBookmark bookmark) {
        NetworkHelper.sendPacketToServer(Journals.MSG_SERVER_MAKE_MAP, data -> data.writeNbt(bookmark.toTag()));
    }

    public static boolean isTakingPhoto() {
        return photoTicks > 0;
    }

    public static void forcePhotoTicks() {
        photoTicks = MAX_PHOTO_TICKS;
    }

    private void updateJournal(@Nullable CompoundTag tag) {
        if (tag != null) {
            ClientHelper.getPlayer().ifPresent(player -> journal = JournalData.fromNbt(player, tag));
        }
    }

    private void processPacketFromServer(Minecraft client, Consumer<Minecraft> clientCallback) {
        client.execute(() -> {
            LocalPlayer player = client.player;
            if (player == null) return;
            clientCallback.accept(client);
        });
    }

    private void handleTakingPhoto(Minecraft client) {
        if (isTakingPhoto() && client.options.keyAttack.isDown()) {
            forcePhotoTicks();
        }

        if (photoTicks > 0) {
            if (bookmarkBeingPhotographed == null) {

                // if there's no bookmark then the ticks are stale, reset them
                photoTicks = 0;

            } else if (++photoTicks > MAX_PHOTO_TICKS) {
                String filename = bookmarkBeingPhotographed.getId() + ".png";

                Screenshot.grab(
                    client.gameDirectory,
                    filename,
                    client.getMainRenderTarget(),
                    component -> {
                        if (client.player != null) {
                            client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                        }

                        // restore the GUI
                        client.options.hideGui = false;

                        // open the journal at the bookmark page
                        client.execute(() -> {
                            client.setScreen(new JournalBookmarkScreen(bookmarkBeingPhotographed));
                            bookmarkBeingPhotographed = null;
                        });
                        LogHelper.debug(this.getClass(), "Screenshot taken");
                    }
                );

                photoTicks = 0;
            }
        }
    }
}
