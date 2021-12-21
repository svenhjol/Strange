package svenhjol.strange.module.journals.photo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.init.StrangeSounds;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.screen.bookmark.JournalBookmarkScreen;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public class TakePhotoHandler {
    private static final int MAX_TICKS = 30;

    private @Nullable Bookmark bookmark;
    private int ticks;

    public void setBookmark(Bookmark bookmark) {
        ClientHelper.getClient().ifPresent(client -> {
            client.setScreen(null);
            client.options.hideGui = true;
        });

        this.bookmark = bookmark.copy();
        this.ticks = 0;
    }

    public void tick(Minecraft client) {
        if (bookmark == null) {
            ticks = 0;
            return;
        }

        // If the player clicks the mouse during the photo countdown then immediately take the photo.
        if (ticks > 0 && client.options.keyAttack.isDown()) {
            ticks = MAX_TICKS;
        }

        // There must be a valid player
        if (client.player == null) return;

        if (++ticks >= MAX_TICKS) {
            var newBookmark = bookmark.copy();
            String filename = "strange_" + newBookmark.getRunes() + ".png";

            Screenshot.grab(
                client.gameDirectory,
                filename,
                client.getMainRenderTarget(),
                component -> {
                    client.player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                    client.options.hideGui = false;

                    client.execute(() -> {
                        client.setScreen(new JournalBookmarkScreen(newBookmark));
                        LogHelper.debug(getClass(), "Screenshot taken for bookmark `" + newBookmark.getRunes() + "`");
                    });
                }
            );

            ticks = 0;
            bookmark = null;
        }
    }
}
