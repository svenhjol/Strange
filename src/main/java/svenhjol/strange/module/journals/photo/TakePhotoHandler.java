package svenhjol.strange.module.journals.photo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.Journals;
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

    public void tick(Minecraft mc) {
        if (bookmark == null) {
            ticks = 0;
            return;
        }

        // If the player clicks the mouse during the photo countdown then immediately take the photo.
        if (ticks > 0 && mc.options.keyAttack.isDown()) {
            ticks = MAX_TICKS;
        }

        // There must be a valid player.
        if (mc.player == null) return;

        if (++ticks >= MAX_TICKS) {
            var newBookmark = bookmark.copy();

            takePhoto(mc, newBookmark.getRunes(), () -> {
                mc.player.playSound(Journals.SCREENSHOT_SOUND, 1.0F, 1.0F);
                mc.options.hideGui = false;
                mc.setScreen(new JournalBookmarkScreen(newBookmark));
            });

            ticks = 0;
            bookmark = null;
        }
    }

    public static void takePhoto(Minecraft mc, String runes, Runnable onTake) {
        String filename = "strange_" + runes + ".png";

        // There must be a valid player.
        if (mc.player == null) return;

        Screenshot.grab(
            mc.gameDirectory,
            filename,
            mc.getMainRenderTarget(),
            component -> {
                LogHelper.debug(Strange.MOD_ID, TakePhotoHandler.class, "Screenshot taken: `" + filename + "`");
                mc.execute(onTake);
            }
        );
    }
}
