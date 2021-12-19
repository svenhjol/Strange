package svenhjol.strange.module.journals;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.journals2.PageTracker.Page;

import java.util.Optional;
import java.util.function.Consumer;

@ClientModule(module = Journals.class)
public class JournalsClient extends CharmModule {
    private static final int MAX_PHOTO_TICKS = 30;

    private KeyMapping keyBinding;
    private static JournalData journal;

    public static JournalBookmark bookmarkBeingPhotographed;
    public static int photoTicks = 0;

    /**
     * Always use this method to reference the current player's journal data on the client.
     * If you need to synchronise it, call sendSyncJournal() or sendOpenJournal() to sync and open
     */
    public static Optional<JournalData> getJournalData() {
        return Optional.ofNullable(journal);
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
//        if (isTakingPhoto() && client.options.keyAttack.isDown()) {
//            forcePhotoTicks();
//        }
//
//        if (photoTicks > 0) {
//            if (bookmarkBeingPhotographed == null) {
//
//                // if there's no bookmark then the ticks are stale, reset them
//                photoTicks = 0;
//
//            } else if (++photoTicks > MAX_PHOTO_TICKS) {
//                String filename = bookmarkBeingPhotographed.getId() + ".png";
//
//                Screenshot.grab(
//                    client.gameDirectory,
//                    filename,
//                    client.getMainRenderTarget(),
//                    component -> {
//                        if (client.player != null) {
//                            client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
//                        }
//
//                        // restore the GUI
//                        client.options.hideGui = false;
//
//                        // open the journal at the bookmark page
//                        client.execute(() -> {
//                            client.setScreen(new JournalBookmarkScreen(bookmarkBeingPhotographed));
//                            bookmarkBeingPhotographed = null;
//                        });
//                        LogHelper.debug(this.getClass(), "Screenshot taken");
//                    }
//                );
//
//                photoTicks = 0;
//            }
//        }
    }
}
